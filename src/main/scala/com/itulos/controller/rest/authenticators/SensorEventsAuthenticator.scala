package com.itulos.controller.rest.authenticators

import com.itulos.controller.dao.SensorTokenDaoComponent
import com.itulos.controller.domain.User
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing._
import spray.routing.authentication._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Authentication mechanism based on access token to use only when submitting sensor events.
 * This token is usually static and never changes. This way sensor events senders perform little static operations
 * Not very secure though
 */
trait SensorEventsAuthenticator extends SensorTokenDaoComponent {
  val sensorTokenDao = new SensorTokenDao

  /**
   * Authenticate a sender using senderToken parameter
   */
  def sensorTokenAuthenticator: ContextAuthenticator[Option[User]] = {
    ctx => {
      val tempToken = ctx.request.uri.query.get("sensorToken")
      doAuthWithSensorToken(tempToken)
    }
  }

  private def doAuthWithSensorToken(token: Option[String]): Future[Authentication[Option[User]]] = {
    if (token == None) {
      return Future(Left(AuthenticationFailedRejection(CredentialsMissing, List())))
    }

    // Get actual token
    val actualSensorToken = sensorTokenDao.getToken
    Future {
      Either.cond(actualSensorToken.get.token == token.get, None, AuthenticationFailedRejection(CredentialsRejected, List()))
    }
  }


}