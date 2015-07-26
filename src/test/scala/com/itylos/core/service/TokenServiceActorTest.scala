package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain.{Token, User}
import com.itylos.core.exception.{TryToLogOutAnotherUserException, TryToUpdateAnotherUserException}
import com.itylos.core.service.protocol._
import org.joda.time.DateTimeUtils
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

class TokenServiceActorTest extends TestKit(ActorSystem("testsystem")) with ImplicitSender with DefaultTimeout
with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll with BeforeAndAfterEach {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Create the actor to test
  val actorRef = TestActorRef(Props(new TokenServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val user = new User(Some("userOid"), "userName", "userEmail",  "webPass", "alarmPass")

  val tokenStr = "tokenStr"
  val tokenData = Token("userOid", "tokenStr", 1000L)

  override def beforeEach(): Unit = {
    reset(tokenDao)
  }

  "A TokenServiceActor" must {
    "generate token for a user and delete expired" in {
      actorRef ! GenerateTokenRq(user)
      verify(tokenDao).save(any())
      verify(tokenDao).deleteExpiredTokens(1000L)
      expectMsgClass(classOf[GenerateTokenRs])
    }
    "deactivate token" in {
      when(tokenDao.getToken(tokenStr)).thenReturn(Some(tokenData))
      actorRef ! DeactivateToken(user, tokenStr)
      verify(tokenDao).deleteToken(tokenStr)
      expectMsg(DeactivateTokenRs())
    }
    "update token's expire time for a user" in {
      when(tokenDao.getToken(tokenStr)).thenReturn(Some(tokenData))
      actorRef ! UpdateTokenExpireTime(user, tokenStr)
      verify(tokenDao).save(any())
      verify(tokenDao).deleteToken(tokenStr)
      expectMsgClass(classOf[GenerateTokenRs])
    }
    "throw exception while deactivating token and token data does not exist " in {
      when(tokenDao.getToken(tokenStr)).thenReturn(None)
      intercept[TryToLogOutAnotherUserException] {
        actorRef.receive(DeactivateToken(user, tokenStr))
      }
      expectNoMsg()
    }
    "throw exception while deactivating token and token's user id is not the same as user's id" in {
      val tokenData = Token("falseUserId", "tokenStr", 1000L)
      when(tokenDao.getToken(tokenStr)).thenReturn(Some(tokenData))
      intercept[TryToLogOutAnotherUserException] {
        actorRef.receive(DeactivateToken(user, tokenStr))
      }
      expectNoMsg()
    }
    "throw exception while updating token expire time and token data do not exist " in {
      when(tokenDao.getToken(tokenStr)).thenReturn(None)
      intercept[TryToUpdateAnotherUserException] {
        actorRef.receive(UpdateTokenExpireTime(user, tokenStr))
      }
      expectNoMsg()
    }
    "throw exception while updating token expire time and token's user id is not the same as user's id" in {
      val tokenData = Token("falseUserId", "tokenStr", 1000L)
      when(tokenDao.getToken(tokenStr)).thenReturn(Some(tokenData))
      intercept[TryToUpdateAnotherUserException] {
        actorRef.receive(UpdateTokenExpireTime(user, tokenStr))
      }
      expectNoMsg()
    }

  }

}
