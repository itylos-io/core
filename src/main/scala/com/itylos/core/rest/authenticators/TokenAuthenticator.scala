package com.itylos.core.rest.authenticators

import com.itylos.core.dao.{TokenDaoComponent, UserDaoComponent}
import com.itylos.core.domain.User
import org.joda.time.DateTime
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing._
import spray.routing.authentication._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Authentication mechanism based on access token parameter
 */
trait TokenAuthenticator extends TokenDaoComponent with UserDaoComponent {
  val tokenDao = new TokenDao


  /**
   * Authenticate a user using token parameter
   */
  def tokenAuthenticator: ContextAuthenticator[User] = {
    ctx => {
      val tempToken = ctx.request.uri.query.get("token")
      doAuthWithTemporaryToken(tempToken)
    }
  }

  private def doAuthWithTemporaryToken(token: Option[String]): Future[Authentication[User]] = {

    if (token == None) {
      return Future(Left(AuthenticationFailedRejection(CredentialsMissing, List())))
    }

    // If supplied token exists we move on
    val tokenData = tokenDao.getToken(token.get)
    val nowTime = new DateTime().getMillis
    Future {
      Either.cond(tokenData != None && tokenData.get.expireTime > nowTime,
        userDao.getUserByObjectId(tokenData.get.userId).get, AuthenticationFailedRejection(CredentialsRejected, List()))
    }
  }


}