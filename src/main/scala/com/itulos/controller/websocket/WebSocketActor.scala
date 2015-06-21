package com.itulos.controller.websocket

import akka.actor._
import com.itulos.controller.rest.dto.SystemOSStatsDto
import com.itulos.controller.service.protocol._
import com.itulos.controller.websocket.ItulosEvents.{SystemStatsMessage, AlarmStatusUpdateMessage, NewSensorEventMessage, UnmonitoredChangesMessage}
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

  val clients = mutable.ListBuffer[WebSocket]()

  import com.itulos.controller.websocket.ItulosEventsJsonProtocol._
  import spray.json._

  override def preStart() {
    log.info("Starting web socket actor...")
  }

  override def receive = {

    // --- New event from Itulos Services --- //
    case Event(event) =>
      event match {
        case alarmStatusRs: AlarmStatusRs =>
          val msg = AlarmStatusUpdateMessage(message = event.asInstanceOf[AlarmStatusRs])
          clients.foreach(ws => ws.send(msg.toJson.toString()))
        case sensorEventRs: GetSensorEventsRs =>
          val msg = NewSensorEventMessage(message = event.asInstanceOf[GetSensorEventsRs].sensorEvents.head)
          clients.foreach(ws => ws.send(msg.toJson.toString()))
        case systemStats: SystemOSStatsDto =>
          val msg = SystemStatsMessage(message =systemStats)
          clients.foreach(ws => ws.send(msg.toJson.toString()))
        case _ =>
          val msg = UnmonitoredChangesMessage()
          clients.foreach(ws => ws.send(msg.toJson.toString()))
      }

    // --- New event from Itulos Services --- //
    case UnMonitoredEvent() =>
      val msg = UnmonitoredChangesMessage()
      clients.foreach(ws => ws.send(msg.toJson.toString()))

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
