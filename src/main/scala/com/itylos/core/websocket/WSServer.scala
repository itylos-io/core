package com.itylos.core.websocket


import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import com.itylos.core.service.protocol._
import org.java_websocket.WebSocket
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

import scala.collection.mutable

/**
 * Web Socket server
 * @param port the port to listen for incoming connection
 */
class WSServer(val port: Int)(implicit system: ActorSystem)
  extends WebSocketServer(new InetSocketAddress(port)) {
  private val log = Logging.getLogger(system, this)

  private val reactors = mutable.Map[String, ActorRef]()

  final def forResource(descriptor: String, reactor: Option[ActorRef]) {
    log.info("Registering actor [{}] to [{}] ", reactor.get.path, descriptor)
    reactor match {
      case Some(actor) => reactors += ((descriptor, actor.asInstanceOf[ActorRef]))
      case None => reactors -= descriptor
    }
  }

  final override def onMessage(ws: WebSocket, msg: String) {
    if (null != ws) {
      reactors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! Message(ws, msg)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }

  final override def onOpen(ws: WebSocket, hs: ClientHandshake) {
    if (null != ws) {
      reactors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! Open(ws, hs)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }

  final override def onClose(ws: WebSocket, code: Int, reason: String, external: Boolean) {
    if (null != ws) {
      reactors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! Terminate(ws, code, reason, external)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }

  final override def onError(ws: WebSocket, ex: Exception) {
    if (null != ws) {
      reactors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! Error(ws, ex)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }
}