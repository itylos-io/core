package com.itylos.core.service

import akka.actor.ActorContext
import com.itylos.core.service.protocol._

/**
 * Send notifications through webSockets, pushBullet, webHooks
 */
trait NotificationsHelper {
  val PUSH_BULLET_DEVICES_ENDPOINT = "https://api.pushbullet.com/v2/pushes"

  /**
   * Notify all registered services
   * @param context the ActorContext
   * @param message the message to be sent to all registrants
   */
  def notifyAll(context: ActorContext, message: NotificationsProtocol): Unit = {
    notifyViaWebSocket(context, Event(message))
    notifyViaWebHooks(context, message)
    notifyViaPushBullet(context, message)
    notifySoundService(context, message)
    notifyStatisticsService(context,message)
  }

  /**
   * Send notifications through web socket
   */
   def notifyViaWebSocket(context: ActorContext, message: WebSocketProtocol): Unit = {
    context.actorSelection("/user/webSocketActor") ! message
  }

  /**
   * Send notifications through push bullet
   */
  private def notifyViaPushBullet(context: ActorContext, message: NotificationsProtocol): Unit = {
    context.actorSelection("/user/pushBulletServiceActor") ! message
  }

  /**
   * Send notifications through webHooks
   */
  private def notifyViaWebHooks(context: ActorContext, message: NotificationsProtocol): Unit = {
    context.actorSelection("/user/webHooksServiceActor") ! message
  }

  /**
   * Notify sound service for event
   */
  private def notifySoundService(context: ActorContext, message: NotificationsProtocol): Unit = {
    context.actorSelection("/user/soundServiceActor") ! message
  }

  /**
   * Notify sound service for event
   */
  private def notifyStatisticsService(context: ActorContext, message: NotificationsProtocol): Unit = {
    context.actorSelection("/user/statisticsActor") ! message
  }


}
