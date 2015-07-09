package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.exception.UserExistsException
import com.itylos.core.rest.dto.UserDto
import com.itylos.core.service.protocol._
import org.joda.time.DateTimeUtils
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.UserServiceActor]]
 */
class UserServiceActorTest extends TestKit(ActorSystem("testActorSystem")) with ImplicitSender
with DefaultTimeout with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll
with BeforeAndAfterEach {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Create the actor to test
  val actorRef = TestActorRef(Props(new UserServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val user = new User(Some("userOid"), "userName", "userEmail", List(), "webPass", "alarmPass")
  val defaultAdminUser = User(None, "admin", "admin@myhome.com", List(), "123456", "123456", 1000, isAdmin = true)

  // Setup expected responses
  val userDtos = List(new UserDto(user), new UserDto(defaultAdminUser))

  // Reset DAOs
  override def beforeEach(): Unit = {
    reset(userDao)
  }

  "A UserServiceActor" must {
    "should create default user if not users exist" in {
      when(userDao.getAllUsers).thenReturn(List())
      actorRef ! LoadAdminUser()
      verify(userDao).save(defaultAdminUser)
    }
    "should not create default user if default user exists" in {
      when(userDao.getAllUsers).thenReturn(List(defaultAdminUser))
      actorRef ! LoadAdminUser()
      verify(userDao, times(0)).save(defaultAdminUser)
    }
    "should not create user if email is already registered" in {
      when(userDao.getUserByEmail(user.email)).thenReturn(Some(user))
      intercept[UserExistsException] {
        actorRef.receive(CreateUserRq(user))
      }
    }
    "should create new user" in {
      when(userDao.getAllUsers).thenReturn(List(user, defaultAdminUser))
      when(userDao.getUserByEmail(user.email)).thenReturn(None)
      actorRef ! CreateUserRq(user)
      verify(userDao).save(user)
      expectMsg(GetUsersRs(userDtos))
    }
    "should check if user exists before updating user" in {
      when(userDao.getAllUsers).thenReturn(List(user, defaultAdminUser))
      when(userDao.getUserByObjectId(user.oid.get)).thenReturn(None)
      actorRef ! UpdateUserRq(user)
      verify(userDao, times(0)).update(user)
      expectMsg(GetUsersRs(userDtos))
    }
    "should update user" in {
      when(userDao.getAllUsers).thenReturn(List(user, defaultAdminUser))
      when(userDao.getUserByObjectId(user.oid.get)).thenReturn(Some(user))
      actorRef ! UpdateUserRq(user)
      verify(userDao).update(user)
      expectMsg(GetUsersRs(userDtos))
    }
    "should delete user" in {
      when(userDao.getAllUsers).thenReturn(List(user, defaultAdminUser))
      actorRef ! DeleteUserRq(user.oid.get)
      verify(userDao).deleteUserByObjectId(user.oid.get)
      expectMsg(GetUsersRs(userDtos))
    }
    "should get all users" in {
      when(userDao.getAllUsers).thenReturn(List(user, defaultAdminUser))
      actorRef ! GetUsersRq()
      expectMsg(GetUsersRs(userDtos))
    }
    "should get user" in {
      when(userDao.getUserByObjectId(user.oid.get)).thenReturn(Some(user))
      actorRef ! GetUserRq(user.oid.get)
      expectMsg(GetUserRs(new UserDto(user)))
    }
  }

}
