package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao._
import com.itylos.core.service.protocol.AlarmTriggeredNotification

import scalaj.http.{Http, HttpOptions}

/**
 * Companion object to properly initiate [[com.itylos.core.service.SmsServiceActor]]
 */
object SmsServiceActor {
  def props(): Props = {
    Props(new SmsServiceActor() with AlarmStatusComponent with SettingsComponent {
      val alarmStatusDao = new AlarmStatusDao
      val settingsDao = new SettingsDao
    })
  }
}

/**
 * An actor responsible for sending SMS alerts through twillio or nexmo service
 */
class SmsServiceActor extends Actor with ActorLogging {
  this: AlarmStatusComponent with SettingsComponent =>

  val SMS_TEXT = "Alert from Itylos Home Security!"
  var NEXMO_ENDPOINT = "https://rest.nexmo.com/sms/json"

  override def preStart() {
    log.info("Starting sms service...")
  }

  def receive = {

    // --- Send email for alarm violation --- //
    case AlarmTriggeredNotification() =>
      val alarmStatus = alarmStatusDao.getAlarmStatus.get
      if (!alarmStatus.smsSent && settingsDao.getSettings.get.nexmoSettings.isEnabled) {
        alarmStatus.smsSent = true
        alarmStatusDao.update(alarmStatus)
        sendSmsThroughNexmo()
      }
  }


  def sendSmsThroughNexmo(): Unit = {
    val smsSettings = settingsDao.getSettings.get.nexmoSettings
    NEXMO_ENDPOINT = smsSettings.nexmoEndpoint

    smsSettings.mobilesToNotify.foreach(mobile => {
      log.info("Sending sms to [{}]", mobile)
      Http(NEXMO_ENDPOINT)
        .param("api_key", smsSettings.nexmoKey)
        .param("api_secret", smsSettings.nexmoSecret)
        .param("from", mobile)
        .param("to", mobile)
        .param("text", SMS_TEXT)
        .header("Content-Type", "application/json")
        .option(HttpOptions.readTimeout(10000)).asString
      log.info("Sms sent to [{}]", mobile)
    })
  }


}