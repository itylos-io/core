package com.itulos.controller.rest

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{OneForOneStrategy, Props, _}
import com.itulos.controller.exception.CustomException
import com.itulos.controller.rest.PerRequestHandler.WithProps
import com.itulos.controller.rest.dto.{AlarmStatusDto, Metadata, RootResponse}
import com.itulos.controller.service.protocol.Protocol
import org.json4s.{FieldSerializer, DefaultFormats, Formats}
import spray.http.StatusCodes._
import spray.httpx.Json4sSupport
import spray.routing.RequestContext

import scala.concurrent.duration._

/**
 * Implements a per request actor handler
 */
trait PerRequestHandler extends Actor with Json4sSupport with ActorLogging {

  import context._

  /* Use to mix Spray's Marshalling Support with json4s */
  implicit def json4sFormats: Formats = DefaultFormats +    FieldSerializer[AlarmStatusDto]()

  /* Timeout for each request */
  setReceiveTimeout(10.seconds)

  def r: RequestContext

  def target: ActorRef

  def message: Protocol


  target ! message

  def receive = {
    case res: Protocol =>
      r.complete(OK, RootResponse(response = res))
      stop(self)
    case ReceiveTimeout =>
      r.complete(RequestTimeout)
      stop(self)
      throw new RuntimeException("Timeout!")
  }

  /**
   * Catch custom exception and response with custom message.
   * Stop actor after an exception
   */
  override val supervisorStrategy =
    OneForOneStrategy(1, 5.seconds) {
      case e: CustomException =>
        r.complete(e.statusCode, RootResponse(Metadata(e.code, Some(e.message), Some("todo")), None))
        stop(self)
        Stop
      case ex =>
        log.error(ex.getMessage)
        r.complete(InternalServerError, RootResponse(Metadata(999, Some("Unknown Sever Error"), Some("todo")), None))
        stop(self)
        Stop

    }
}

object PerRequestHandler {

  case class WithProps(r: RequestContext, props: Props, message: Protocol) extends PerRequestHandler {
    lazy val target = context.actorOf(props)
  }

}

trait PerRequestCreator {
  def perRequest(actorRefFactory: ActorRefFactory, r: RequestContext, props: Props, message: Protocol) =
    actorRefFactory.actorOf(Props(new WithProps(r, props, message)))
}
