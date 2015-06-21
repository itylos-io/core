package com.itulos.controller

import _root_.akka.io.IO
import _root_.spray.can.Http
import akka.actor.{ActorSystem, Props}
import com.itulos.controller.domain.{OPEN, SensorEvent, SensorToken}
import com.itulos.controller.rest.RestServiceActor
import com.itulos.controller.service._
import com.itulos.controller.service.protocol._
import com.itulos.controller.websocket.{WSServer, WebSocketActor}
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime


/**
 * Entry point for API
 */
object Api {

  def main(args: Array[String]) {
    implicit val system = ActorSystem("ItulosActorSystem")

    // Start web socket
    val echo = system.actorOf(WebSocketActor.props(), "webSocketActor")
    val wsServer = new WSServer(9997)
    wsServer.forResource("/events", Some(echo))
    wsServer.start()

    // Start the rest api
    val rest = system.actorOf(Props[RestServiceActor])
    IO(Http) ! Http.Bind(rest, interface = "0.0.0.0", port = 8081)

    // Load initial config
    triggerInitialConfig(system)
    createPermanentActors(system)
  }

  private def createPermanentActors(system: ActorSystem) {
    system.actorOf(AlarmWatcherServiceActor.props(), "alarmWatcher")
    system.actorOf(EmailServiceActor.props(), "emailService")
    system.actorOf(SmsServiceActor.props(), "smsService")
    system.actorOf(SystemStatsServiceActor.props(), "statsService")
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

    // Load sensor token
    val sensorTokenStr = ConfigFactory.load().getString("sensorToken")
    val sensorTokenServiceActor = system.actorOf(SensorTokenServiceActor.props())
    val sensorToken = SensorToken(None, sensorTokenStr, new DateTime().getMillis)
    sensorTokenServiceActor ! UpdateSensorToken(sensorToken, false)

    // Load settings
    val settingsActor = system.actorOf(SettingsServiceActor.props())
    settingsActor ! SetupInitialSettingRq()

    val s = new SensorEvent()
    s.status = OPEN
    system.actorOf(SoundServiceActor.props()) ! NewSensorEvent(s)
  }

}
