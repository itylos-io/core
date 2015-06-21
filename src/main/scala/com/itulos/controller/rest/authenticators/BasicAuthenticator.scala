package com.itulos.controller.rest.authenticators

import com.itulos.controller.dao.UserDaoComponent
import com.itulos.controller.domain.User
import org.joda.time.DateTime
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}
import spray.routing.authentication._
import spray.routing.directives.AuthMagnet

import scala.concurrent.{ExecutionContext, Future}

/**
 * Authenticator that uses basic authentication workflow
 */
trait BasicAuthenticator extends UserDaoComponent {
  val userDao = new UserDao


  def basicUserAuthenticator(implicit ec: ExecutionContext): AuthMagnet[AuthInfo] = {
    def validateUser(userPass: Option[UserPass]): Option[AuthInfo] = {
      for {
        p <- userPass
        user <- userDao.getUserByEmail(p.user)
        if user.webPasswordMatches(p.pass)
      } yield new AuthInfo(user)
    }


    def authenticator(userPass: Option[UserPass]): Future[Option[AuthInfo]] = Future {
      validateUser(userPass)
    }

    BasicAuth(authenticator _, realm = "Itulos Controller API")
  }
}
