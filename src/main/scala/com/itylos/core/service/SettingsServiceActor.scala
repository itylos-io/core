package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao.SettingsComponent
import com.itylos.core.domain.{KerberosInstance, PushBulletDevice, Settings}
import com.itylos.core.rest.dto.SettingsDto
import com.itylos.core.service.protocol._

/**
 * Companion object to properly initiate [[com.itylos.core.service.SettingsServiceActor]]
 */
object SettingsServiceActor {
  def props(): Props = {
    Props(new SettingsServiceActor() with SettingsComponent {
      val settingsDao = new SettingsDao
    })
  }
}

/**
 * An actor responsible for managing [[com.itylos.core.domain.Settings]]
 */
class SettingsServiceActor extends Actor with ActorLogging {
  this: SettingsComponent =>

  def receive = {

    // --- Setup initial settings --- //
    case SetupInitialSettingRq() =>
      var settings = settingsDao.getSettings
      if (settings == None) {
        log.info("Initiating settings for first time")
        settings = Some(Settings())
        settingsDao.save(settings.get)
      }
      sender() ! GetSystemSettingsRs(new SettingsDto(settings.get))

    // --- Update nexmo settings --- //
    case UpdateNexmoSettingsRq(nexmoSettings) =>
      val settings = settingsDao.getSettings.get
      settingsDao.deleteSettings()
      settings.nexmoSettings = nexmoSettings
      settingsDao.save(settings)
      sender() ! GetSystemSettingsRs(getSettingsAsDto)

    // --- Update email settings --- //
    case UpdateEmailSettingsRq(emailSettings) =>
      val settings = settingsDao.getSettings.get
      settingsDao.deleteSettings()
      settings.emailSettings = emailSettings
      settingsDao.save(settings)
      sender() ! GetSystemSettingsRs(getSettingsAsDto)

    // --- Update system settings --- //
    case UpdateSystemSettingsRq(systemSettings) =>
      val settings = settingsDao.getSettings.get
      settingsDao.deleteSettings()
      settings.systemSettings = systemSettings
      settingsDao.save(settings)
      sender() ! GetSystemSettingsRs(getSettingsAsDto)

    // --- Update PushBulletSettings --- //
    case UpdatePushBulletSettingsRq(pushBulletSettings) =>
      val settings = settingsDao.getSettings.get
      settingsDao.deleteSettings()
      settings.pushBulletSettings = pushBulletSettings
      settingsDao.save(settings)
      sender() ! GetSystemSettingsRs(getSettingsAsDto)

    // --- Update KerberosSettings --- //
    case UpdateKerberosSettingsRq(kerberosSettings) =>
      val settings = settingsDao.getSettings.get
      settingsDao.deleteSettings()
      settings.kerberosSettings = kerberosSettings
      settingsDao.save(settings)
      // TODO kill actor
      context.actorOf(SensorServiceActor.props()) ! UpdateKerberosSensors(settingsDao.getSettings.get.kerberosSettings.kerberosInstances)
      context.actorSelection("/user/kerberosManager") ! ConfigureKerberosInstances(settingsDao.getSettings.get.kerberosSettings.kerberosInstances)

      sender() ! GetSystemSettingsRs(getSettingsAsDto)

    // --- Update WebHookSettings --- //
    case UpdateWebHookSettingsRq(webHookSettings) =>
      val settings = settingsDao.getSettings.get
      settingsDao.deleteSettings()
      settings.webHookSettings = webHookSettings
      settingsDao.save(settings)
      sender() ! GetSystemSettingsRs(getSettingsAsDto)

    // --- Get settings --- //
    case GetSystemSettingsRq() =>
      sender() ! GetSystemSettingsRs(getSettingsAsDto)

    // --- Get PushBullet devices directly from PushBullet API --- //
    case GetPushBulletDevicesRq(accessToken) =>
      val activeDevices = getPushBulletDevices(accessToken)
      sender() ! GetPushBulletDevicesRs(activeDevices)

  }


  /**
   * Get PushBullet devices associated to provided access token
   * @param accessToken the access token of the user
   * @return the active devices
   */
  def getPushBulletDevices(accessToken: String): List[PushBulletDevice] = {
    val PUSH_BULLET_DEVICES_ENDPOINT = "https://api.pushbullet.com/v2/devices"
    import spray.json._
    import DefaultJsonProtocol._

    import scalaj.http._
    // Make http request to PushBullet
    val result = Http(PUSH_BULLET_DEVICES_ENDPOINT)
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer " + accessToken)
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString

    // Deserialize
    val activeDevicesJson = result.body.parseJson.asJsObject.fields("devices").convertTo[List[JsObject]]
    val activeDevices = for (device <- activeDevicesJson) yield {
      if (device.fields("active").toString().toBoolean) {
        PushBulletDevice(isEnabled = true,
          device.fields("iden").asInstanceOf[JsString].value,
          device.fields("nickname").asInstanceOf[JsString].value)
      } else {
        PushBulletDevice(isEnabled = false, "iden", "nickname")
      }
    }
    activeDevices.filter(p => p.isEnabled)
  }

  /**
   * @return Settings as SettingsDto
   */
  def getSettingsAsDto: SettingsDto = {
    val settings = settingsDao.getSettings
    new SettingsDto(settings.get)
  }

}