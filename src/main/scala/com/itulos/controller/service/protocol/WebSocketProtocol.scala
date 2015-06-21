package com.itulos.controller.service.protocol

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake

/**
 * Messages needed for web socket management
 */
sealed trait WebSocketProtocol extends Protocol

/**
 * Message that indicates an event has occurred but web sockets does not monitor it
 */
case class UnMonitoredEvent() extends WebSocketProtocol

/**
 * Message that indicates an event has occurred and has to be sent to all clients
 * @param event the event
 */
case class Event(event: Protocol) extends WebSocketProtocol

/**
 * Message that indicates a message has been received from the web socket
 * @param ws the WebSocket
 * @param msg the Message
 */
case class Message(ws: WebSocket, msg: String) extends WebSocketProtocol

/**
 * Message that indicates the a new web socket connection
 * @param ws the WebSocket
 */
case class Open(ws: WebSocket, hs: ClientHandshake) extends WebSocketProtocol

/**
 * Message that indicates the termination of a web socket connection
 * @param ws the WebSocket
 */
case class Terminate(ws: WebSocket, code: Int, reason: String, external: Boolean) extends WebSocketProtocol

/**
 * Message that indicates an exception has been occurred during web socket connection
 * @param ws the WebSocket
 * @param ex the Exception
 */
case class Error(ws: WebSocket, ex: Exception) extends WebSocketProtocol


