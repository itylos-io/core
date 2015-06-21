package com.itulos.controller.service

import akka.actor.ActorContext
import com.itulos.controller.service.protocol.WebSocketProtocol


trait WebSocketNotifier {

  def notifyWebSocket(context: ActorContext, message: WebSocketProtocol): Unit = {
    context.actorSelection("/user/webSocketActor") ! message
  }

}
