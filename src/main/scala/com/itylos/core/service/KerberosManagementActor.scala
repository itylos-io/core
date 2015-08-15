package com.itylos.core.service

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao._
import com.itylos.core.domain.{KerberosEventImages, KerberosInstance, KerberosSettings, Settings}
import com.itylos.core.exception.{ARMED, CouldNotChangeKerbrosConfigException}
import com.itylos.core.service.protocol._
import org.joda.time.DateTime
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scalaj.http.{Http, HttpOptions}

/**
 * Companion object to properly initiate [[com.itylos.core.service.KerberosManagementActor]]
 */
object KerberosManagementActor {
  def props(): Props = {
    Props(new KerberosManagementActor() with SettingsComponent with KerberosEventImagesComponent {
      val settingsDao = new SettingsDao
      val kerberosEventImagesDao = new KerberosEventImagesDao
    })
  }
}

/**
 * An actor responsible for enabling/disabling kerberos instances, registering webhook url to a kerberos instance
 * and managing events received from kerberos instances
 */
class KerberosManagementActor extends Actor with ActorLogging {
  this: SettingsComponent with KerberosEventImagesComponent =>

  // 10 seconds without event from kerberos we consider it as motion stopped event
  val KERBEROS_INACTIVE_TIMEOUT = 10000
  val ITYLOS_KERBEROS_EVENTS_HANDLER = "/api/v1/kerberos/events"
  var kerberosLatestEvents: Map[String, Long] = Map()
  var kerberosEventsIds: Map[String, String] = Map()

  override def preStart() {
    import scala.concurrent.duration._
    context.system.scheduler.schedule(1.seconds, 5.seconds, self, CheckKerberosInactivity())
  }

  def receive = {

    // --- get instance name from a kerberos instance --- //
    case GetKerberosInstanceRq(ip, username, password) =>
      val instanceName = getKerberosInstanceName(ip, username, password, settingsDao.getSettings.get.kerberosSettings)
      val kerberosInstance = new KerberosInstance(instanceName, ip, username, password)
      sender() ! GetKerberosInstanceRs(kerberosInstance)

    // --- add itylos' webhook url to each kerberos instance --- //
    case ConfigureKerberosInstances(instances) =>
      val kerberosSettings = settingsDao.getSettings.get.kerberosSettings
      instances.foreach(instance => {
        configureKerberosInstance(instance, kerberosSettings)
      })

    // --- enable/disable kerberos instances --- //
    case UpdateKerberosInstances(status) =>
      val settings = settingsDao.getSettings.get
      if (settings.kerberosSettings.isEnabled) {
        val newStatus = if (status == ARMED) "true" else "false"
        settings.kerberosSettings.kerberosInstances.foreach(instance => {
          log.info("Updating status of kerberos instance [{}]", instance.instanceName)
          changeKerberosInstanceStatus(newStatus, settings, instance)
        })
      }

    // --- handle event from kerberos instance and convert to sensor event --- //
    case MotionDetected(instanceName, pathToImage, ip) =>
      val pathToImageWithProxy = "http://" + getCoreApiIp + ":18081/api/v1/kerberos/image_proxy?imageUrl=" +
        "http://" + ip + "/capture/" + pathToImage
      if (!kerberosLatestEvents.contains(instanceName)) {
        val kerberosEventId = UUID.randomUUID().toString
        kerberosLatestEvents = kerberosLatestEvents + (instanceName -> new DateTime().getMillis)
        kerberosEventsIds = kerberosEventsIds + (instanceName -> kerberosEventId)
        addImageToKerberosEvent(kerberosEventsIds(instanceName), pathToImageWithProxy)
        val settings = settingsDao.getSettings.get
        simulateSensorEvent(instanceName, "1", kerberosEventId, settings)
      } else {
        kerberosLatestEvents = kerberosLatestEvents + (instanceName -> new DateTime().getMillis)
        addImageToKerberosEvent(kerberosEventsIds(instanceName), pathToImageWithProxy)
      }

    // --- Since kerberos does not send motion stopped events we need to simulate a motion stopped event
    case CheckKerberosInactivity() =>
      val settings = settingsDao.getSettings.get
      kerberosLatestEvents.foreach(kerberosEvent => {
        if (kerberosEvent._2 + KERBEROS_INACTIVE_TIMEOUT < new DateTime().getMillis) {
          kerberosLatestEvents = kerberosLatestEvents.-(kerberosEvent._1)
          simulateSensorEvent(kerberosEvent._1, "0", kerberosEventsIds(kerberosEvent._1), settings)
        }
      })

  }

  /**
   * When a new kerberos event is received, add the associated image to the corresponding kerberos event
   * @param kerberosEventId the existng kerberos event id
   * @param imageUrl the new image url to add
   */
  private def addImageToKerberosEvent(kerberosEventId: String, imageUrl: String): Unit = {
    val kerberosEventImages = kerberosEventImagesDao.getImagesForKerberosEvent(kerberosEventId)
    if (kerberosEventImages == None) {
      val newKerberosEventImages = new KerberosEventImages(None, kerberosEventId, List(imageUrl))
      kerberosEventImagesDao.save(newKerberosEventImages)
    } else {
      kerberosEventImages.get.addImageUrlToEvents(imageUrl)
      kerberosEventImagesDao.update(kerberosEventImages.get)
    }
  }

  private def configureKerberosInstance(instance: KerberosInstance, settings: KerberosSettings): Unit = {
    log.info("Configuring kerberos instance [{}]", instance.instanceName)
    import spray.json._
    import DefaultJsonProtocol._

    // Get io devices
    val ioDevicesUri = "http://" + instance.ip + settings.kerberosInstanceIoDevicesEndpoint
    val rs = Http(ioDevicesUri).auth(instance.username, instance.password).header("Content-Type", "application/json")
      .header("Charset", "UTF-8").option(HttpOptions.readTimeout(10000)).asString.body.parseJson.asJsObject
    val ioDevices = rs.fields("devices").convertTo[List[String]]

    // Add webhook if it's not in the io devices
    if (!ioDevices.contains("Webhook")) {
      log.info("Configuring io devices for kerberos instance [{}]", instance.instanceName)
      val updatedIoDevices = (ioDevices ::: List("Webhook")).toJson
      val postData = s"""{"value":$updatedIoDevices}"""
      val rs = Http(ioDevicesUri).auth(instance.username, instance.password)
        .postData(postData).method("put")
        .header("Content-Type", "application/json").header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000)).asString
      if (!rs.is2xx) {
        log.error("Got response [{}]")
        throw new CouldNotChangeKerbrosConfigException(instance.instanceName)
      }
    }

    // Configure webhook url
    log.info("Configuring webhooks for kerberos instance [{}]", instance.instanceName)
    val webhookDevicesUri = "http://" + instance.ip + settings.kerberosInstanceWebhookEndpoint
    val webhookUrl = "http://" + getCoreApiIp + ":18081/api/v1/kerberos/events"
    val postData = s"""{"url":"$webhookUrl"}"""
    val webhookRs = Http(webhookDevicesUri).auth(instance.username, instance.password)
      .postData(postData).method("put").header("Content-Type", "application/json").header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString
    if (!webhookRs.is2xx) {
      log.error("Got response [{}]")
      throw new CouldNotChangeKerbrosConfigException(instance.instanceName)
    }
  }

  /**
   * Send sensorEvent to core API
   */
  private def simulateSensorEvent(sensorId: String, sensorStatus: String, kerberosEventId: String, settings: Settings) {
    val url = "http://localhost:18081/api/v1/sensors/events?sensorToken=" + settings.systemSettings.accessToken
    val payload = s"""{"status":"$sensorStatus","sensorId":"$sensorId","kerberosEventId":"$kerberosEventId"}"""
    Http(url).postData(payload).header("Content-Type", "application/json")
      .header("Charset", "UTF-8").option(HttpOptions.readTimeout(10000)).asString
  }

  /**
   * Make http to a specific kerberos instance to get instance's name
   */
  private def getKerberosInstanceName(ip: String, user: String, password: String, settings: KerberosSettings): String = {
    val uri = "http://" + ip + settings.kerberosInstanceNameEndpoint
    val rs = Http(uri).auth(user, password).header("Content-Type", "application/json").header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString
    if (!rs.is2xx) throw new CouldNotChangeKerbrosConfigException(ip)
    rs.body.parseJson.asJsObject.fields("name").asInstanceOf[spray.json.JsString].value
  }


  /**
   * Make http to a specific kerberos instance to enable/disable it
   * @param status the new status (true/false)
   */
  private def changeKerberosInstanceStatus(status: String, settings: Settings, kerberosInstance: KerberosInstance): Unit = {
    val uri = "http://" + kerberosInstance.ip + settings.kerberosSettings.kerberosConditionUpdateEndpoint
    val delay = settings.systemSettings.delayToArm * 1000
    val postData = s"""{"active":"$status","delay":"$delay"}"""
    val rs = Http(uri).auth(kerberosInstance.username, kerberosInstance.password)
      .postData(postData).method("put")
      .header("Content-Type", "application/json").header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString
    if (!rs.is2xx) throw new CouldNotChangeKerbrosConfigException(kerberosInstance.instanceName)
  }


  /**
   * @return the IP of the core API server
   */
  def getCoreApiIp: String = {
    var ip = ""
    val interfaces = java.net.NetworkInterface.getNetworkInterfaces
    while (interfaces.hasMoreElements) {
      val iface = interfaces.nextElement()
      // filters out 127.0.0.1 and inactive interfaces
      if (!iface.isLoopback && iface.isUp) {
        val addresses = iface.getInetAddresses
        while (addresses.hasMoreElements) {
          val addr = addresses.nextElement
          ip = addr.getHostAddress
          if (!ip.contains(":")) {
            return ip
          }
        }
      }
    }
    ip
  }

}