package com.itulos.controller.service

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.Timeout
import com.itulos.controller.dao.TokenDaoComponent
import com.itulos.controller.domain.{Token, User}
import com.itulos.controller.exception.{TryToLogOutAnotherUserException, TryToUpdateAnotherUserException}
import com.itulos.controller.rest.dto.AuthToken
import com.itulos.controller.service.protocol._
import org.joda.time.DateTime

import scala.concurrent.duration._

object TokenServiceActor {
  def props(): Props = {
    Props(new TokenServiceActor() with TokenDaoComponent {
      val tokenDao = new TokenDao
    })
  }
}

/**
 * An actor responsible for managing tokens
 */
class TokenServiceActor extends Actor with ActorLogging {
  this: TokenDaoComponent =>

  val TOKEN_TTL = 60 // minutes

  implicit val timeout = Timeout(5.seconds)

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
   * Create a token for a specified user
   * @param user the user to create token for
   * @return the created Token
   */
  private def createUserToken(user: User): Token = {
    val tokenStr = UUID.randomUUID().toString
    val expirationTime = new DateTime().plusMinutes(TOKEN_TTL)
    Token(user.oid.get, tokenStr, expirationTime.getMillis)
  }


}