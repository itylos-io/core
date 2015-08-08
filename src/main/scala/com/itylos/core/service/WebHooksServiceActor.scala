package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao._
import com.itylos.core.rest.dto.SensorEventDto
import com.itylos.core.service.protocol._
import com.itylos.core.websocket.ItylosNotificationEvents.{AlarmTriggeredMessage, NewSensorEventMessage, UpdatedAlarmStatusMessage}

import scalaj.http.{Http, HttpOptions}


/**
 * A companion object to properly initiate [[com.itylos.core.service.WebHooksServiceActor]]
 */
object WebHooksServiceActor {
  def props(): Props = {
    Props(new WebHooksServiceActor() with SettingsComponent with AlarmStatusComponent {
      val settingsDao = new SettingsDao
      val alarmStatusDao = new AlarmStatusDao
    })
  }
}

/**
 * An actor responsible for sending notifications through WebHooks
 */
class WebHooksServiceActor extends Actor with ActorLogging {
  this: SettingsComponent with AlarmStatusComponent =>

  var ARE_WEBHOOKS_ENABLED = false
  var URIS: List[String] = List()

  import com.itylos.core.websocket.ItylosEventsJsonProtocol._
  import spray.json._

  override def preStart() {
    log.info("Starting webHooks notification service")
  }

  def receive = {

    // --- Notify for sensor event --- //
    case NewSensorEventNotification(sensor, sensorEvent,kerberosImages) =>
      updateSettings()
      val msg = NewSensorEventMessage(message = new SensorEventDto(sensorEvent, sensor))
      notifyUris(msg.toJson.toString())

    // --- Notify for change in alarm status --- //
    case UpdatedAlarmStatusNotification(alarmStatus) =>
      updateSettings()
      val msg = UpdatedAlarmStatusMessage(message = alarmStatus)
      notifyUris(msg.toJson.toString())

    // --- Notify for alarm violation --- //
    case AlarmTriggeredNotification() =>
      val msg = AlarmTriggeredMessage()
      notifyUris(msg.toJson.toString())
  }

  /**
   * Update the settings every time because they change on the fly
   */
  def updateSettings(): Unit = {
    ARE_WEBHOOKS_ENABLED = settingsDao.getSettings.get.webHookSettings.isEnabled
    URIS = settingsDao.getSettings.get.webHookSettings.uris
  }

  /**
   * Send notification via WebHooks
   * @param payload the payload to be sent
   */
  private def notifyUris(payload: String) {
    if (!ARE_WEBHOOKS_ENABLED) return
    URIS.foreach(uri => {
      Http(uri).postData(payload)
        .header("Content-Type", "application/json")
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000)).asString
    })
  }


}
