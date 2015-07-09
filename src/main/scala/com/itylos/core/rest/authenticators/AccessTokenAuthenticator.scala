package com.itylos.core.rest.authenticators

import com.itylos.core.dao.SettingsComponent
import com.itylos.core.domain.User
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing._
import spray.routing.authentication._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Authentication mechanism based on access token to use only when submitting sensor events.
 * This token is usually static and never changes. This way sensor events senders perform little static operations
 */
trait AccessTokenAuthenticator extends SettingsComponent {
  val settingsDao = new SettingsDao

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
    val actualSensorToken = settingsDao.getSettings.get.systemSettings.accessToken
    Future {
      Either.cond(actualSensorToken == token.get, None, AuthenticationFailedRejection(CredentialsRejected, List()))
    }
  }


}