package com.itylos.core.service

import java.io.{BufferedInputStream, FileInputStream}
import javax.sound.sampled._

import akka.actor._
import com.itylos.core.dao.SettingsComponent
import com.itylos.core.domain.OPEN
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

  def receive = {

    // --- New sensor event --- //
    case NewSensorEventNotification(sensor, sensorEvent) =>
      if (settingsDao.getSettings.get.systemSettings.playSoundsForSensorEvents) {
        if (sensorEvent.status == OPEN) playSound("open.wav") else playSound("closed.wav")
      }

    // --- Alarm triggered --- //
    case AlarmTriggeredNotification() =>
      if (settingsDao.getSettings.get.systemSettings.playSoundsForTriggeredAlarm)
        playSound("closed.wav")

    // --- Alarm status updated --- //
    case UpdatedAlarmStatusNotification(alarmStatus) =>
      if (settingsDao.getSettings.get.systemSettings.playSoundsForAlarmStatusUpdates) {
        if (alarmStatus.currentStatus == "ARMED") playSound("open.wav") else playSound("closed.wav")
      }

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