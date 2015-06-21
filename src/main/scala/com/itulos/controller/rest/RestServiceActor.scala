package com.itulos.controller.rest

import akka.actor.Actor
import spray.routing.HttpService


class RestServiceActor extends Actor with HttpService with CustomExceptionHandler with CustomRejectionHandler
with RestRoutes {

  def actorRefFactory = context

  def receive = runRoute(handleRejections(customRejectionHandler)
    (handleExceptions(customExceptionHandler)(userRoutes ~ sensorEventsRoutes ~ sensorRoutes ~ zoneRoutes
      ~ settingsRoutes ~ alarmRoutes)))

}
