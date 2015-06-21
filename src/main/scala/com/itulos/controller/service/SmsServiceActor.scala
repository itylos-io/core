package com.itulos.controller.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao._
import com.itulos.controller.service.protocol.NotifyForAlarmViolation
import com.nexmo.messaging.sdk.NexmoSmsClient
import com.nexmo.messaging.sdk.messages.TextMessage

object SmsServiceActor {
  def props(): Props = {
    Props(new SmsServiceActor() with AlarmStatusComponent with SettingsComponent {
      val alarmStatusDao = new AlarmStatusDao
      val settingsDao = new SettingsDao
    })
  }
}

/**
 * An actor responsible for managing sms alerts
 */
class SmsServiceActor extends Actor with ActorLogging {
  this: AlarmStatusComponent with SettingsComponent =>

  val SMS_TEXT = "Alert from Itulos Home Security!"

  override def preStart() {
    log.info("Starting sms service...")
  }


  def receive = {
    // --- Send email for alarm violation --- //
    case NotifyForAlarmViolation() =>
      val alarmStatus = alarmStatusDao.getAlarmStatus.get
      if (!alarmStatus.smsSent) {
        alarmStatus.smsSent = true
        alarmStatusDao.update(alarmStatus)
        sendSmsAlerts()
      }
  }


  def sendSmsAlerts(): Unit = {
    val smsSettings = settingsDao.getSettings.get.nexmoSettings
    if (!smsSettings.isEnabled) return

    smsSettings.mobilesToNotify.foreach(mobile => {
      log.info("Sending sms to [{}]", mobile)
      try {
        val client = new NexmoSmsClient(smsSettings.nexmoKey, smsSettings.nexmoSecret)
        val message = new TextMessage(mobile, mobile, SMS_TEXT)
        client.submitMessage(message)
      } catch {
        case e: Exception =>
      }

      log.info("Sms sent to [{}]", mobile)
    })
  }


}