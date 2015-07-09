package com.itylos.core.service

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.Timeout
import com.itylos.core.dao.TokenDaoComponent
import com.itylos.core.domain.{Token, User}
import com.itylos.core.exception.{TryToLogOutAnotherUserException, TryToUpdateAnotherUserException}
import com.itylos.core.rest.dto.AuthToken
import com.itylos.core.service.protocol._
import org.joda.time.DateTime

import scala.concurrent.duration._

/**
 * Companion object to properly initiate [[com.itylos.core.service.TokenServiceActor]]
 */
object TokenServiceActor {
  def props(): Props = {
    Props(new TokenServiceActor() with TokenDaoComponent {
      val tokenDao = new TokenDao
    })
  }
}

/**
 * An actor responsible for managing [[com.itylos.core.domain.Token]] instances
 */
class TokenServiceActor extends Actor with ActorLogging {
  this: TokenDaoComponent =>

  implicit val timeout = Timeout(5.seconds)
  val TOKEN_TTL = 60 // minutes

  def receive = {
    // --- Generate a new token --- //
    case GenerateTokenRq(user) =>
      log.info("Generating new access token for user [{}]", user.email)
      val token = createUserToken(user)
      tokenDao.save(token)
      tokenDao.deleteExpiredTokens(new DateTime().getMillis)
      sender() ! GenerateTokenRs(AuthToken(token.token, token.expireTime))

    // --- Deactivate/Delete token --- //
    case DeactivateToken(user, token) =>
      log.info("Deleting access token for user [{}]", user.email)
      val tokenData = tokenDao.getToken(token)
      if (tokenData == None || (tokenData != None && tokenData.get.userId != user.oid.get)) {
        throw new TryToLogOutAnotherUserException()
      }
      tokenDao.deleteToken(token)
      sender() ! DeactivateTokenRs()

    // --- Update token's expire time --- //
    case UpdateTokenExpireTime(user, token) =>
      log.info("Updating TTL for token [{}]", token)
      val tokenData = tokenDao.getToken(token)
      if (tokenData == None || (tokenData != None && tokenData.get.userId != user.oid.get)) {
        throw new TryToUpdateAnotherUserException()
      }
      tokenDao.deleteToken(token)
      val newToken = createUserToken(user)
      tokenDao.save(newToken)
      sender() ! GenerateTokenRs(AuthToken(newToken.token, newToken.expireTime))
  }

  /**
   * Create a [[com.itylos.core.domain.Token]] for a specified [[com.itylos.core.domain.User]]
   * @param user the [[com.itylos.core.domain.User]] to create token for
   * @return the newly created [[com.itylos.core.domain.Token]]
   */
  private def createUserToken(user: User): Token = {
    val tokenStr = UUID.randomUUID().toString
    val expirationTime = new DateTime().plusMinutes(TOKEN_TTL)
    Token(user.oid.get, tokenStr, expirationTime.getMillis)
  }


}