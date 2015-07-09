package com.itylos.core.service

import akka.actor.{Actor, ActorRef}

/**
 * An actor to test messages between actors using TestProbe
 * @param to
 */
class ForwardActor(to: ActorRef) extends Actor {
  def receive = {
    case x => to.forward(x)
  }
}
