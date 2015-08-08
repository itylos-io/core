package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.rest.dto.AlarmStatusDto
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
 * Specs for [[com.itylos.core.service.WebHooksServiceActor]]
 */
class WebhooksServiceActorTest extends TestKit(ActorSystem("testsystem")) with ImplicitSender with DefaultTimeout
with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll with BeforeAndAfterEach {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Create the actor to test
  val actorRef = TestActorRef(Props(new WebHooksServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val sensorId = "200"
  val sensorOId = "sensorOId"
  val sensorEvent = new SensorEvent(None, sensorId, 1, 100, None, 1000L)
  val sensor = Sensor(Some(sensorOId), sensorId, "sName", "sDesc", "sLoc", "1", isActive = true, 1000L)

  var settings = new Settings()

  // Start mock server
  val mockServer = startClientAndServer(8887)
  mockServer
    .when(request().withPath("/trigger"))
    .respond(response().withHeaders(new Header("Content-Type", "application/json")).withBody("ok"))

  override def beforeEach(): Unit = {
    settings = new Settings()
    settings.webHookSettings.isEnabled = true
    settings.webHookSettings.uris = List("http://localhost:8887/trigger")
    when(settingsDao.getSettings).thenReturn(Some(settings))
  }

  override def afterAll(): Unit = {
    mockServer.stop()
  }

  "A WebHooksServiceActor" must {
    "send not post events when webhooks are disabled" in {
      val thisSettings = new Settings()
      thisSettings.webHookSettings.isEnabled = false
      settings.webHookSettings.uris = List("http://localhost:8887/trigger")
      when(settingsDao.getSettings).thenReturn(Some(thisSettings))

      val dto = AlarmStatusDto("ENABLED")
      actorRef ! UpdatedAlarmStatusNotification(dto)
      mockServer.verify(
        request().withMethod("POST").withPath("/trigger")
          .withBody("{\"eventType\":\"updatedAlarmStatus\",\"message\":{\"currentStatus\":\"ENABLED\"}}")
        , VerificationTimes.exactly(0)
      )
    }
    "send events to uris in case of NewSensorEventNotification" in {
      actorRef ! NewSensorEventNotification(sensor, sensorEvent)
      mockServer.verify(
        request().withMethod("POST").withPath("/trigger")
          .withBody("{" +
          "\"eventType\":\"newSensorEvent\"," +
          "\"message\":{\"oid\":\"\"," +
          "\"sensorId\":\"200\"," +
          "\"sensorName\":\"sName\"," +
          "\"sensorLocation\":\"sLoc\"," +
          "\"sensorTypeId\":\"1\"," +
          "\"status\":1," +
          "\"batteryLevel\":100," +
          "\"dateOfEvent\":1000," +
          "\"dateOfEventH\":\"1970-01-01T00:00:01.000Z\"" +
          "}}")
        , VerificationTimes.exactly(1)
      )
    }
    "send events to uris in case of UpdatedAlarmStatusNotification" in {
      val dto = AlarmStatusDto("ENABLED")
      actorRef ! UpdatedAlarmStatusNotification(dto)
      mockServer.verify(
        request().withMethod("POST").withPath("/trigger")
          .withBody("{\"eventType\":\"updatedAlarmStatus\",\"message\":{\"currentStatus\":\"ENABLED\"}}")
        , VerificationTimes.exactly(1)
      )
    }
    "send events to uris in case of AlarmTriggeredNotification" in {
      actorRef ! AlarmTriggeredNotification()
      mockServer.verify(
        request().withMethod("POST").withPath("/trigger")
          .withBody("{\"eventType\":\"alarmTriggered\"}")
        , VerificationTimes.exactly(1)
      )
    }
  }
}
