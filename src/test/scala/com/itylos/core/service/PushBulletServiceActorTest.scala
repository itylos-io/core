package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.rest.dto.{AlarmStatusDto, UserDto}
import com.itylos.core.service.protocol.{AlarmTriggeredNotification, NewSensorEventNotification, UpdatedAlarmStatusNotification}
import org.joda.time.DateTimeUtils
import org.mockito.Mockito._
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.verify.VerificationTimes
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.PushBulletServiceActor]]
 */
class PushBulletServiceActorTest extends TestKit(ActorSystem("testsystem")) with ImplicitSender with DefaultTimeout
with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll with BeforeAndAfterEach {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Create the actor to test
  val actorRef = TestActorRef(Props(new PushBulletServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val sensorId = "200"
  val sensorOId = "sensorOId"
  val sensorEvent = new SensorEvent(None, sensorId, OPEN, 100, 1000L)
  val sensor = Sensor(Some(sensorOId), sensorId, "sName", "sDesc", "sLoc", "1", isActive = true, 1000L)
  val pushbulletDevices = List(PushBulletDevice(isEnabled = true, "iden", "name"))
  val user = User(None, "admin", "admin@myhome.com", List(), "123456", "123456", 1000, isAdmin = true)
  val alarmStatusDto = AlarmStatusDto("ENABLED", Some(new UserDto(user)))
  var settings = new Settings()
  val alarmStatus = new AlarmStatus()
  when(alarmStatusDao.getAlarmStatus).thenReturn(Some(alarmStatus))

  // Start mock server
  var mockServer = startClientAndServer(10050)
  mockServer
    .when(request().withPath("/trigger"))
    .respond(response().withHeaders(
    new Header("Content-Type", "application/json")).withBody("ok"))

  override def beforeEach(): Unit = {
    reset(settingsDao)
    settings = new Settings()
    settings.pushBulletSettings.isEnabled = true
    settings.pushBulletSettings.notifyForAlarmsStatusUpdates = true
    settings.pushBulletSettings.notifyForSensorEvents = true
    settings.pushBulletSettings.notifyForAlarms = true
    settings.pushBulletSettings.devices = pushbulletDevices
    settings.pushBulletSettings.pushBulletEndpoint = "http://localhost:10050/trigger"
    when(settingsDao.getSettings).thenReturn(Some(settings))

    mockServer.reset()
    mockServer
      .when(request().withPath("/trigger"))
      .respond(response().withHeaders(
      new Header("Content-Type", "application/json")).withBody("ok"))

  }

  override def afterAll(): Unit ={
    mockServer.stop()
  }

  "A PushBulletServiceActor" must {
    "not notify for alarm updates when notify for alarm updates is disabled" in {
      settings.pushBulletSettings.notifyForAlarmsStatusUpdates = false
      actorRef ! UpdatedAlarmStatusNotification(alarmStatusDto)
      mockServer.verify(
        request().withMethod("POST").withPath("/trigger")
          .withBody("{\"device_iden\":\"iden\",\"type\":\"note\",\"title\":\"Itylos.io\",\"body\":\"admin changed alarm status to enabled\"}")
        , VerificationTimes.exactly(0)
      )
    }
    "notify for alarm updates when notify for alarm updates is enabled" in {
      actorRef ! UpdatedAlarmStatusNotification(alarmStatusDto)
      mockServer.verify(
        request().withMethod("POST").withPath("/trigger").withHeader( new Header("Authorization", "Bearer "+settings.pushBulletSettings.accessToken))
          .withBody("{\"device_iden\":\"iden\",\"type\":\"note\",\"title\":\"Itylos.io\",\"body\":\"admin changed alarm status to enabled\"}")
        , VerificationTimes.exactly(1)
      )
    }
    "not notify for sensor events when sensor event notification is disabled" in {
      settings.pushBulletSettings.notifyForSensorEvents = false
      actorRef ! NewSensorEventNotification(sensor, sensorEvent)
      mockServer.verify(
        request().withMethod("POST").withPath("/trigger").withHeader( new Header("Authorization", "Bearer "+settings.pushBulletSettings.accessToken))
          .withBody("{\"device_iden\":\"iden\",\"type\":\"note\",\"title\":\"Itylos.io\",\"body\":\"sName is now open\"}")
        , VerificationTimes.exactly(0)
      )
    }
    "notify for sensor events when sensor event notification is enabled" in {
      settings.pushBulletSettings.notifyForSensorEvents = true
      actorRef ! NewSensorEventNotification(sensor, sensorEvent)
      mockServer.verify(
        request().withMethod("POST").withPath("/trigger").withHeader( new Header("Authorization", "Bearer "+settings.pushBulletSettings.accessToken))
          .withBody("{\"device_iden\":\"iden\",\"type\":\"note\",\"title\":\"Itylos.io\",\"body\":\"sName is now open\"}")
        , VerificationTimes.exactly(1)
      )
    }
    "not notify for alarm when alarm notification is disabled" in {
      settings.pushBulletSettings.notifyForAlarms = false
      actorRef ! AlarmTriggeredNotification()
      mockServer.verify(
        request().withMethod("POST").withPath("/trigger").withHeader( new Header("Authorization", "Bearer "+settings.pushBulletSettings.accessToken))
          .withBody("{\"device_iden\":\"iden\",\"type\":\"note\",\"title\":\"Itylos.io\",\"body\":\"Alarm has been triggered !!!\"}")
        , VerificationTimes.exactly(0)
      )
    }
    "notify for alarm when alarm notification is enabled" in {
      actorRef ! AlarmTriggeredNotification()
      mockServer.verify(
        request().withMethod("POST").withPath("/trigger").withHeader( new Header("Authorization", "Bearer "+settings.pushBulletSettings.accessToken))
          .withBody("{\"device_iden\":\"iden\",\"type\":\"note\",\"title\":\"Itylos.io\",\"body\":\"Alarm has been triggered !!!\"}")
        , VerificationTimes.exactly(1)
      )
    }
  }
}
