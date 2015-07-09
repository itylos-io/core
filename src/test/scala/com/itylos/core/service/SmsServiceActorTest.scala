package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.service.protocol.AlarmTriggeredNotification
import org.joda.time.DateTimeUtils
import org.mockito.Mockito._
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.verify.VerificationTimes
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.SmsServiceActor]]
 */
class SmsServiceActorTest extends TestKit(ActorSystem("testsystem")) with ImplicitSender with DefaultTimeout
with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll with BeforeAndAfterEach {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Create the actor to test
  val actorRef = TestActorRef(Props(new SmsServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val alarmStatus = new AlarmStatus()


  var settings = new Settings()

  // Start mock server
  val mockServer = startClientAndServer(7578)
  mockServer
    .when(request().withPath("/trigger"))
    .respond(response().withHeaders(new Header("Content-Type", "application/json")).withBody("ok"))

  override def beforeEach(): Unit = {
    reset(settingsDao)
    reset(alarmStatusDao)
    settings = new Settings()
    settings.nexmoSettings.isEnabled = true
    settings.nexmoSettings.nexmoEndpoint = "http://localhost:7578/trigger"
    settings.nexmoSettings.mobilesToNotify = List("00306978787877")
    when(settingsDao.getSettings).thenReturn(Some(settings))
    when(alarmStatusDao.getAlarmStatus).thenReturn(Some(alarmStatus))
  }

  override def afterAll(): Unit ={
    mockServer.stop()
  }

  "A SmsServiceActor" must {
    "send not send sms if already sent" in {
      alarmStatus.smsSent = true
      settings.nexmoSettings.isEnabled = true
      actorRef ! AlarmTriggeredNotification()
      verify(alarmStatusDao, times(0)).update(alarmStatus)
    }
    "send not send sms if nexmo is disabled" in {
      alarmStatus.smsSent = false
      settings.nexmoSettings.isEnabled = false
      actorRef ! AlarmTriggeredNotification()
      verify(alarmStatusDao, times(0)).update(alarmStatus)
    }
    "send send sms" in {
      alarmStatus.smsSent = false
      settings.nexmoSettings.isEnabled = true
      actorRef ! AlarmTriggeredNotification()

      mockServer.verify(
        request().withMethod("GET").withPath("/trigger")
          .withQueryStringParameter("api_key", settings.nexmoSettings.nexmoKey)
          .withQueryStringParameter("api_secret", settings.nexmoSettings.nexmoSecret)
          .withQueryStringParameter("from", "00306978787877")
          .withQueryStringParameter("to", "00306978787877")
          .withQueryStringParameter("text", "Alert from Itylos Home Security!")
        , VerificationTimes.exactly(1)
      )
      val updatedAlarmStatus = new AlarmStatus()
      updatedAlarmStatus.smsSent=true
      verify(alarmStatusDao, times(1)).update(updatedAlarmStatus)
    }
  }
}
