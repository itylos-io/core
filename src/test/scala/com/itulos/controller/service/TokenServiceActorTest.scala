package com.itulos.controller.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itulos.controller.StopSystemAfterAll
import com.itulos.controller.dao.TestEnvironmentRepos
import com.itulos.controller.domain.{Token, User}
import com.itulos.controller.exception.{TryToUpdateAnotherUserException, TryToLogOutAnotherUserException}
import com.itulos.controller.service.protocol._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

class TokenServiceActorTest extends TestKit(ActorSystem("testsystem")) with ImplicitSender with DefaultTimeout
with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    reset(tokenDao)
  }

  val actorRef = TestActorRef(Props(new TokenServiceActor() with TestEnvironmentRepos {}))

  val user = new User()
  user.oid = Some("userId")

  val tokenStr = "tokenStr"
  val tokenData = Token("userId", "tokenStr", 1000L)

  "A TokenServiceActor" must {
    "generate token for a user" in {
      actorRef ! GenerateTokenRq(user)
      verify(tokenDao).save(any())
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
    "throw exception when token data do not exist when deactivating token" in {
      when(tokenDao.getToken(tokenStr)).thenReturn(None)
      intercept[TryToLogOutAnotherUserException] {
        actorRef.receive(DeactivateToken(user, tokenStr))
      }
      expectNoMsg()
    }
    "throw exception when token user id is not the same as user's id when deactivating token" in {
      val tokenData = Token("falseUserId", "tokenStr", 1000L)
      when(tokenDao.getToken(tokenStr)).thenReturn(Some(tokenData))
      intercept[TryToLogOutAnotherUserException] {
        actorRef.receive(DeactivateToken(user, tokenStr))
      }
      expectNoMsg()
    }
    "throw exception when token data do not exist when updating token expire time" in {
      when(tokenDao.getToken(tokenStr)).thenReturn(None)
      intercept[TryToUpdateAnotherUserException] {
        actorRef.receive(UpdateTokenExpireTime(user, tokenStr))
      }
      expectNoMsg()
    }
    "throw exception when token user id is not the same as user's id when updating token expire time" in {
      val tokenData = Token("falseUserId", "tokenStr", 1000L)
      when(tokenDao.getToken(tokenStr)).thenReturn(Some(tokenData))
      intercept[TryToUpdateAnotherUserException] {
        actorRef.receive(UpdateTokenExpireTime(user, tokenStr))
      }
      expectNoMsg()
    }

  }

}
