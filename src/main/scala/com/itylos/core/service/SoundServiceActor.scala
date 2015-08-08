package com.itylos.core.service

import java.io.{BufferedInputStream, FileInputStream}
import javax.sound.sampled._

import akka.actor._
import com.itylos.core.dao.SettingsComponent
import com.itylos.core.service.protocol.{AlarmTriggeredNotification, NewSensorEventNotification, UpdatedAlarmStatusNotification}

/**
 * Companion object to properly initiate [[com.itylos.core.service.SoundServiceActor]]
 */
object SoundServiceActor {
  def props(): Props = {
    Props(new SoundServiceActor() with SettingsComponent {
      val settingsDao = new SettingsDao
    })
  }
}

/**
 * An actor responsible for playing sounds on server side
 */
class SoundServiceActor extends Actor with ActorLogging {
  this: SettingsComponent =>

  override def preStart() {
    log.info("Starting sound service")
  }

  var PLAY_SOUNDS_FOR_SENSOR_EVENTS = false
  var PLAY_SOUNDS_FOR_TRIGGERED_ALARM = false
  var PLAY_SOUNDS_FOR_ALARM_STATUS_UPDATES = false

  def receive = {

    // --- New sensor event --- //
    case NewSensorEventNotification(sensor, sensorEvent,kerberosImages) =>
      if (PLAY_SOUNDS_FOR_SENSOR_EVENTS) {
        if (sensorEvent.status == 1) playSound("sensor_event_0.wav") else playSound("sensor_event_1.wav")
      }

    // --- Alarm triggered --- //
    case AlarmTriggeredNotification() =>
      if (PLAY_SOUNDS_FOR_TRIGGERED_ALARM)
        playSound("alarm_triggered.wav")

    // --- Alarm status updated --- //
    case UpdatedAlarmStatusNotification(alarmStatus) =>
      if (settingsDao.getSettings.get.systemSettings.playSoundsForAlarmStatusUpdates) {
        if (alarmStatus.currentStatus == "ARMED") playSound("system_armed.wav") else playSound("system_disarmed.wav")
      }
  }

  /**
   * Update the settings because they change on the fly
   */
  def updateSettings(): Unit = {
    val settings = settingsDao.getSettings.get.systemSettings
    PLAY_SOUNDS_FOR_SENSOR_EVENTS = settings.playSoundsForSensorEvents
    PLAY_SOUNDS_FOR_TRIGGERED_ALARM = settings.playSoundsForTriggeredAlarm
    PLAY_SOUNDS_FOR_ALARM_STATUS_UPDATES = settings.playSoundsForAlarmStatusUpdates
  }


  def playSound(file: String) {
    val stream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(System.getProperty("user.home") + "/" + file)))
    val clip = javax.sound.sampled.AudioSystem.getClip
    if (clip != null || clip.isOpen || clip.isActive || clip.isRunning) clip.close()
    clip.open(stream)
    clip.addLineListener(new LineListener() {
      def update(myLineEvent: LineEvent) {
        if (myLineEvent.getType == LineEvent.Type.STOP) {
          myLineEvent.getLine.close()
          clip.stop()
          stream.close()
          clip.close()
        }
      }
    })
    clip.start()
  }


}