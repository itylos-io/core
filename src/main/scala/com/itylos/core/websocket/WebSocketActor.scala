package com.itylos.core.websocket

import akka.actor._
import com.itylos.core.rest.dto.{SensorEventDto, SystemOSStatsDto}
import com.itylos.core.service.protocol._
import com.itylos.core.websocket.ItylosNotificationEvents._
import org.java_websocket.WebSocket

import scala.collection._

object WebSocketActor {
  def props(): Props = {
    Props(new WebSocketActor())
  }
}


/**
 * Actor that handles the websocket requests and sends messages to clients
 */
class WebSocketActor() extends Actor with ActorLogging {

  val KERBEROS_SENSOR_TYPE_ID = "5"
  val clients = mutable.ListBuffer[WebSocket]()

  import com.itylos.core.websocket.ItylosEventsJsonProtocol._
  import spray.json._

  override def preStart() {
    log.info("Starting web socket actor...")
  }

  override def receive = {

    // --- New event from Itulos Services --- //
    case Event(event) =>
      event match {
        // Changes in alarm status
        case updatedAlarmStatus: UpdatedAlarmStatusNotification =>
          val msg = UpdatedAlarmStatusMessage(message = updatedAlarmStatus.alarmStatus)
          clients.foreach(ws => ws.send(msg.toJson.toString()))
        // New Sensor Event
        case sensorEventRs: NewSensorEventNotification =>
          val msg = NewSensorEventMessage(message = new SensorEventDto(sensorEventRs.sensorEvent, sensorEventRs.sensor,sensorEventRs.kerberosImages))
          clients.foreach(ws => ws.send(msg.toJson.toString()))
        // Triggered alarm
        case alarmTriggered: AlarmTriggeredNotification =>
          val msg = AlarmTriggeredMessage()
          clients.foreach(ws => ws.send(msg.toJson.toString()))
        // Updated system stats
        case systemStats: SystemOSStatsDto =>
          val msg = SystemStatsMessage(message = systemStats)
          clients.foreach(ws => ws.send(msg.toJson.toString()))
        // Updated weather conditions
        case weatherConditions: UpdatedWeatherConditionsNotification =>
          val msg = UpdatedWeatherConditionsMessage(message = weatherConditions.weatherConditions)
          clients.foreach(ws => ws.send(msg.toJson.toString()))
      }

    // --- Client registered --- //
    case Open(ws, hs) =>
      log.info("Registering new client to web socket. Client [{}]", ws.getRemoteSocketAddress)
      clients += ws

    // --- Client unregistered --- //
    case Terminate(ws, code, reason, ext) =>
      if (ws != null) {
        log.info("Un-registering client from web socket. Client [{}]", ws.getRemoteSocketAddress)
        clients -= ws
      }

    // --- Error occurred --- //
    case Error(ws, ex) =>
      self ! Terminate(ws, -1, "", false)

    // --- Message from websocket --- //
    case Message(wsa, msg) =>
      clients.foreach(ws => ws.send(s"Itulos can not handle your message [$msg] !"))

  }


}
