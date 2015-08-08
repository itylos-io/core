package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain.{KerberosEventImages, KerberosInstance, Settings}
import com.itylos.core.exception.{ARMED, DISARMED}
import com.itylos.core.service.protocol._
import org.joda.time.DateTimeUtils
import org.mockito.Matchers._
import org.mockito.Mockito
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
class KerberosManagementActorTest extends TestKit(ActorSystem("testsystem")) with ImplicitSender with DefaultTimeout
with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll with BeforeAndAfterEach {

  // Create the actor to test
  val actorRef = TestActorRef(Props(new KerberosManagementActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val kerberos_ip = "localhost:10050"
  val kerberos_user = "root"
  val kerberos_pass = "root"
  val settings = new Settings()
  settings.kerberosSettings.isEnabled = true
  settings.kerberosSettings.kerberosInstances = List(new KerberosInstance("Backyard", "localhost:10050", "root", "root"))


  // Start mock server
  val mockServer = startClientAndServer(10050)

  override def beforeEach(): Unit = {
    reset(settingsDao)
    reset(kerberosEventImagesDao)
    mockServer.reset()
    mockServer.when(request().withPath("/api/v1/io/webhook")).respond(response().withHeaders(
      new Header("Content-Type", "application/json")).withBody("ok"))
    when(settingsDao.getSettings).thenReturn(Some(settings))

  }

  override def afterAll(): Unit = {
    mockServer.stop()
  }

  "A KerberosManagementActor" must {
    "should get instance name from a kerberos instance" in {
      val expectedKerberosInstance = new KerberosInstance("Backyard", "localhost:10050", "root", "root")
      mockServer.when(request().withPath("/api/v1/name")).respond(response().withHeaders(
        new Header("Content-Type", "application/json")).withBody("{\n  \"name\": \"Backyard\"\n}"))
      actorRef ! GetKerberosInstanceRq(kerberos_ip, kerberos_user, kerberos_pass)
      expectMsg(GetKerberosInstanceRs(expectedKerberosInstance))
    }
    "should enable all kerberos instances" in {
      mockServer.when(request().withPath("/api/v1/condition/enabled")).respond(response()
        .withHeaders(new Header("Content-Type", "application/json")).withBody("ok"))
      actorRef ! UpdateKerberosInstances(ARMED)
      mockServer.verify(request().withMethod("PUT").withPath("/api/v1/condition/enabled")
        .withBody("{\"active\":\"true\",\"delay\":\"15000\"}")
      )
    }
    "should disable all kerberos instances" in {
      mockServer.when(request().withPath("/api/v1/condition/enabled")).respond(response()
        .withHeaders(new Header("Content-Type", "application/json")).withBody("ok"))
      actorRef ! UpdateKerberosInstances(DISARMED)
      mockServer.verify(request().withMethod("PUT").withPath("/api/v1/condition/enabled")
        .withBody("{\"active\":\"false\",\"delay\":\"15000\"}")
      )
    }
    "should not change state of kerberos instances if kerberos integration is disabled" in {
      settings.kerberosSettings.isEnabled = false
      mockServer.when(request().withPath("/api/v1/condition/enabled")).respond(response()
        .withHeaders(new Header("Content-Type", "application/json")).withBody("ok"))
      actorRef ! UpdateKerberosInstances(DISARMED)
      mockServer.verify(request().withMethod("PUT").withPath("/api/v1/condition/enabled")
        .withBody("{\"active\":\"true\",\"delay\":\"15000\"}")
        , VerificationTimes.exactly(0)
      )
    }
    "should send sensor event when first motion detected message is receive from kerberos" in {
      val mockServer = startClientAndServer(18081)
      when(kerberosEventImagesDao.getImagesForKerberosEvent(any(classOf[String]))).thenReturn(None)
      actorRef ! MotionDetected("name", "imageUrl", "localhost")
      verify(kerberosEventImagesDao).save(any(classOf[KerberosEventImages]))
      mockServer.verify(request().withMethod("POST").withPath("/api/v1/sensors/events"))
      mockServer.stop()
    }
    "should not send sensor event when second motion detected message is receive from kerberos within timeout" in {
      val tempActorRef = TestActorRef(Props(new KerberosManagementActor() with TestEnvironmentRepos {}))
      // First kerberos event
      when(kerberosEventImagesDao.getImagesForKerberosEvent(any(classOf[String]))).thenReturn(None)
      tempActorRef ! MotionDetected("name", "imageUrl", "localhost")
      verify(kerberosEventImagesDao).save(any(classOf[KerberosEventImages]))

      // Second kerberos event within timeout
      val kerberosEventImages = Mockito.mock(classOf[KerberosEventImages])
      when(kerberosEventImagesDao.getImagesForKerberosEvent(any(classOf[String])))
        .thenReturn(Some(kerberosEventImages))
      tempActorRef ! MotionDetected("name", "imageUrl", "localhost")
      verify(kerberosEventImagesDao).update(any(classOf[KerberosEventImages]))
      verify(kerberosEventImages).addImageUrlToEvents("http://localhost/capture/imageUrl")
    }
    "should send sensor event when not motion message has been detected after timeout" in {
      // Fixed time for easier testing
      DateTimeUtils.setCurrentMillisFixed(1000L)
      val mockServer = startClientAndServer(18081)
      val tempActorRef = TestActorRef(Props(new KerberosManagementActor() with TestEnvironmentRepos {}))
      when(kerberosEventImagesDao.getImagesForKerberosEvent(any(classOf[String]))).thenReturn(None)
      // Send first motion detected event
      tempActorRef ! MotionDetected("name", "imageUrl", "localhost")
      // Wait for timeout and expect second sensor event
      DateTimeUtils.setCurrentMillisFixed(10000000L)
      Thread.sleep(10000)
      mockServer.verify(request().withMethod("POST").withPath("/api/v1/sensors/events"), VerificationTimes.exactly(2))
      mockServer.stop()
    }
    "should not add webhook io device if it already exists" in {
      mockServer.when(request().withPath("/api/v1/io")).respond(response().withHeaders(
        new Header("Content-Type", "application/json")).withBody("{\n\"devices\": [\n\"Disk\",\n\"Webhook\"\n]\n}"))
      actorRef ! ConfigureKerberosInstances(settings.kerberosSettings.kerberosInstances)
      mockServer.verify(request().withMethod("PUT").withPath("/api/v1/io"), VerificationTimes.exactly(0))
    }

  }
}
