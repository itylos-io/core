package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao._
import com.itylos.core.domain.{PushBulletDevice, Sensor, SensorEvent}
import com.itylos.core.service.protocol._

import scalaj.http.{Http, HttpOptions}

/**
 * A companion object to properly initiate [[com.itylos.core.service.PushBulletServiceActor]]
 */
object PushBulletServiceActor {
  def props(): Props = {
    Props(new PushBulletServiceActor() with SettingsComponent with AlarmStatusComponent {
      val settingsDao = new SettingsDao
      val alarmStatusDao = new AlarmStatusDao
    })
  }
}

/**
 * An actor responsible for sending notifications through PushBullet Service
 */
class PushBulletServiceActor extends Actor with ActorLogging {
  this: SettingsComponent with AlarmStatusComponent =>

  var PUSH_BULLET_DEVICES_ENDPOINT = "https://api.pushbullet.com/v2/pushes"
  var PUSH_BULLET_ACCESS_TOKEN = ""
  var NOTIFY_FOR_SENSOR_EVENTS = true
  var NOTIFY_FOR_ALARM_STATUS_UPDATES = false
  var PUSH_BULLET_DEVICES: List[PushBulletDevice] = List()
  var NOTIFY_FOR_ALARMS = false
  var NOTIFY_VIA_PUSHBULLET = false

  override def preStart() {
    log.info("Starting pushBullet notification service...")
  }

  def receive = {

    // --- Notify for sensor event --- //
    case NewSensorEventNotification(sensor, sensorEvent,kerberosImages) =>
      updateSettings()
      if (NOTIFY_VIA_PUSHBULLET && NOTIFY_FOR_SENSOR_EVENTS) {
        val message = createSensorEventMessage(sensorEvent, sensor)
        sendViaPushBullet(message)
      }

    // --- Notify for change in alarm status --- //
    case UpdatedAlarmStatusNotification(alarmStatus) =>
      updateSettings()
      if (NOTIFY_VIA_PUSHBULLET && NOTIFY_FOR_ALARM_STATUS_UPDATES) {
        val message = alarmStatus.user.get.name + " changed alarm status to " + alarmStatus.currentStatus.toLowerCase
        sendViaPushBullet(message)
      }

    // --- Notify for alarm violation --- //
    case AlarmTriggeredNotification() =>
      updateSettings()
      val alarmStatus = alarmStatusDao.getAlarmStatus.get
      if (!alarmStatus.pushBulletNotificationsSent && NOTIFY_FOR_ALARMS && NOTIFY_VIA_PUSHBULLET) {
        alarmStatus.pushBulletNotificationsSent = true
        alarmStatusDao.update(alarmStatus)
        val message = "Alarm has been triggered !!!"
        sendViaPushBullet(message)
      }

  }

  def createSensorEventMessage(sensorEvent: SensorEvent, sensor: Sensor): String = {
    var message = ""
    if (sensor.sensorTypeId == "1" || sensor.sensorTypeId == "2") {
      if (sensorEvent.status == 1) {
        message = sensor.name + " is now open"
      } else {
        message = sensor.name + " is now closed"
      }
    } else if (sensor.sensorTypeId == "3" || sensor.sensorTypeId == "4") {
      if (sensorEvent.status == 1) {
        message = "Movement detected in  " + sensor.name
      } else {
        message = "Movement stopped  in  " + sensor.name
      }
    }
    message
  }

  /**
   * Update the settings because they change on the fly
   */
  def updateSettings(): Unit = {
    val settings = settingsDao.getSettings.get.pushBulletSettings
    PUSH_BULLET_DEVICES_ENDPOINT = settings.pushBulletEndpoint
    NOTIFY_VIA_PUSHBULLET = settings.isEnabled
    PUSH_BULLET_ACCESS_TOKEN = settings.accessToken
    NOTIFY_FOR_SENSOR_EVENTS = settings.notifyForSensorEvents
    NOTIFY_FOR_ALARM_STATUS_UPDATES = settings.notifyForAlarmsStatusUpdates
    PUSH_BULLET_DEVICES = settings.devices.filter(p => p.isEnabled)
    NOTIFY_FOR_ALARMS = settings.notifyForAlarms
  }

  /**
   * Send notification via PushBullet
   * @param message the message to be sent
   */
  private def sendViaPushBullet(message: String) {
    PUSH_BULLET_DEVICES.foreach(device => {
      val payload = makePushBulletPayload(device.iden, message)
      Http(PUSH_BULLET_DEVICES_ENDPOINT).postData(payload)
        .header("Content-Type", "application/json")
        .header("Charset", "UTF-8")
        .header("Authorization", "Bearer " + PUSH_BULLET_ACCESS_TOKEN)
        .option(HttpOptions.readTimeout(10000)).asString
    })
  }

  /**
   * Make the json payload to be sent to PushBullet
   * @param iden the device ID
   * @param body the message of the notification
   * @return the formatted json payload
   */
  private def makePushBulletPayload(iden: String, body: String): String = {
    s"""{"device_iden":"$iden","type":"note","title":"Itylos.io","body":"$body"}"""
  }

}