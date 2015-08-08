package com.itylos.core

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import com.itylos.core.dao.KerberosEventImagesComponent
import com.itylos.core.rest.RestServiceActor
import com.itylos.core.service._
import com.itylos.core.service.protocol._
import com.itylos.core.websocket.{WSServer, WebSocketActor}
import spray.can.Http

/**
 * Entry point for API
 */
object Api {

  def main(args: Array[String]) {
    implicit val system = ActorSystem("ItylosActorSystem")

    // Start web socket
    val echo = system.actorOf(WebSocketActor.props(), "webSocketActor")
    val wsServer = new WSServer(19997)
    wsServer.forResource("/events", Some(echo))
    wsServer.start()

    // Start the rest api
    val rest = system.actorOf(Props[RestServiceActor])
    IO(Http) ! Http.Bind(rest, interface = "0.0.0.0", port = 18081)

    // Create indexes
    new KerberosEventImagesComponent {
      override val kerberosEventImagesDao: KerberosEventImagesDao = new KerberosEventImagesDao()
      kerberosEventImagesDao.createIndex()
    }

    // Load initial config
    triggerInitialConfig(system)
    createPermanentActors(system)

  }

  private def createPermanentActors(system: ActorSystem) {
    system.actorOf(AlarmWatcherServiceActor.props(), "alarmWatcher")
    system.actorOf(KerberosManagementActor.props(), "kerberosManager")
    system.actorOf(EmailServiceActor.props(), "emailService")
    system.actorOf(SmsServiceActor.props(), "smsService")
    system.actorOf(SystemStatsServiceActor.props(), "statsService")
    system.actorOf(PushBulletServiceActor.props(), "pushBulletServiceActor")
    system.actorOf(WebHooksServiceActor.props(), "webHooksServiceActor")
    system.actorOf(SoundServiceActor.props(), "soundServiceActor")
  }

  private def triggerInitialConfig(system: ActorSystem): Unit = {

    // Create alarm status entry
    val alarmService = system.actorOf(AlarmStatusServiceActor.props())
    alarmService ! SetupAlarmStatus()

    // Load admin user
    val userService = system.actorOf(UserServiceActor.props())
    userService ! LoadAdminUser()

    // Load sensor types
    val sensorTypeServiceActor = system.actorOf(SensorTypeServiceActor.props())
    sensorTypeServiceActor ! LoadSensorTypes()

    // Load settings
    val settingsActor = system.actorOf(SettingsServiceActor.props())
    settingsActor ! SetupInitialSettingRq()
  }

}
