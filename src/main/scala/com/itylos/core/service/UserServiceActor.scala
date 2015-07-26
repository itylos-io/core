package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao.UserDaoComponent
import com.itylos.core.domain.User
import com.itylos.core.exception.UserExistsException
import com.itylos.core.rest.dto.UserDto
import com.itylos.core.service.protocol._
import com.typesafe.config.ConfigFactory

/**
 * Companion object to properly initiate [[com.itylos.core.service.UserServiceActor]]
 */
object UserServiceActor {
  def props(): Props = {
    Props(new UserServiceActor() with UserDaoComponent {
      val userDao = new UserDao
    })
  }
}

/**
 * An actor responsible for managing [[com.itylos.core.domain.User]] instances
 */
class UserServiceActor extends Actor with ActorLogging {
  this: UserDaoComponent =>


  def receive = {

    // --- Create admin user if does not exist --- //
    case LoadAdminUser() =>
      if (userDao.getAllUsers.size == 0) {
        log.info("Creating default user...")
        val adminConfig = ConfigFactory.load().getConfig("admin")
        val adminEmail = adminConfig.getString("email")
        val adminWebPass = adminConfig.getString("webPass")
        val adminAlarmPass = adminConfig.getString("alarmPass")
        val adminUser = User(None, "admin", adminEmail,  adminWebPass, adminAlarmPass, isAdmin = true)
        userDao.save(adminUser)
      }

    // --- Create new user --- //
    case CreateUserRq(user) =>
      if (userDao.getUserByEmail(user.email) != None) throw new UserExistsException(user.email)
      userDao.save(user)
      sender ! GetUsersRs(convert2DTOs(userDao.getAllUsers))

    // --- Update new user --- //
    case UpdateUserRq(user) =>
      if (userDao.getUserByObjectId(user.oid.get) != None) userDao.update(user)
      sender ! GetUsersRs(convert2DTOs(userDao.getAllUsers))

    // --- Delete user --- //
    case DeleteUserRq(userId) =>
      userDao.deleteUserByObjectId(userId)
      sender ! GetUsersRs(convert2DTOs(userDao.getAllUsers))

    // --- Get users --- //
    case GetUsersRq() =>
      sender ! GetUsersRs(convert2DTOs(userDao.getAllUsers))

    // --- Get user --- //
    case GetUserRq(userOId) =>
      sender ! GetUserRs(convert2DTOs(List(userDao.getUserByObjectId(userOId).get)).head)
  }

  /**
   * Convert [[com.itylos.core.domain.User]] to [[com.itylos.core.rest.dto.UserDto]]
   * @param users the [[com.itylos.core.domain.User]] objects to convert
   * @return the converted DTO objects
   */
  private def convert2DTOs(users: List[User]): List[UserDto] = {
    for (user <- users) yield new UserDto(user)
  }

}