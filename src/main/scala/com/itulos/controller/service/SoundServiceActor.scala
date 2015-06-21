package com.itulos.controller.service

import java.io.{FileInputStream, BufferedInputStream, File}
import javax.sound.sampled._

import akka.actor._
import com.itulos.controller.domain.OPEN
import com.itulos.controller.service.protocol.NewSensorEvent

object SoundServiceActor {
  def props(): Props = {
    Props(new SoundServiceActor())
  }
}

/**
 * An actor responsible for sounds on server side
 */
class SoundServiceActor extends Actor with ActorLogging {

  def receive = {
    // --- Add sensor event --- //
    case NewSensorEvent(sensorEvent) =>
      if (sensorEvent.status == OPEN) {
        playSound("open.wav")
      } else {
        playSound("closed.wav")
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
          self ! PoisonPill
        }
      }
    })
    clip.start()
  }


}