package com.itulos.controller.service


import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao.UserDaoComponent
import com.itulos.controller.domain.User
import com.itulos.controller.exception.{CannotDeleteAdminException, UserExistsException}
import com.itulos.controller.rest.dto.UserDto
import com.itulos.controller.service.protocol._
import com.typesafe.config.ConfigFactory

object UserServiceActor {
  def props(): Props = {
    Props(new UserServiceActor() with UserDaoComponent {
      val userDao = new UserDao
    })
  }
}

/**
 * An actor responsible for managing users
 */
class UserServiceActor extends Actor with ActorLogging {
  this: UserDaoComponent =>

  val ADMIN_USER_ID = "1"

  def receive = {
    // --- Create admin user if does not exist --- //
    case LoadAdminUser() =>
      if (userDao.getAllUsers.size == 0) {
        log.info("Creating default user...")
        createAdminUser()
      } else {
        log.info("Admin user already configured...")
      }
    // --- Create new user --- //
    case CreateUserRq(user) =>
      if (userDao.getUserByEmail(user.email) == None) {
        userDao.save(user)
        sender ! GetUsersRs(convert2DTOs(userDao.getAllUsers))
      } else {
        throw new UserExistsException(user.email)
      }
    // --- Update new user --- //
    case UpdateUserRq(user)=>
      if (userDao.getUserByObjectId(user.oid.get) != None) {
        userDao.update(user)
      }
      sender ! GetUsersRs(convert2DTOs(userDao.getAllUsers))
    // -- Delete user -- //
    case DeleteUserRq(userId) =>
       userDao.deleteUserByObjectId(userId)
        sender ! GetUsersRs(convert2DTOs(userDao.getAllUsers))
      // -- Get users -- //
    case GetUsersRq() =>
      sender ! GetUsersRs(convert2DTOs(userDao.getAllUsers))
    // -- Get user -- //
    case GetUserRq(userOId) =>
      sender ! GetUserRs(convert2DTOs(List(userDao.getUserByObjectId(userOId).get)).head)
  }

  /**
   * Convert User to UserDto
   * @param users the users to convert
   * @return the converted DTO objects
   */
  private def convert2DTOs(users: List[User]): List[UserDto] = {
    for (user <- users) yield new UserDto(user)
  }

  /**
   * Load admin configuration from properties file and persist to db
   */
  private def createAdminUser(): Unit = {
    val adminConfig = ConfigFactory.load().getConfig("admin")
    val adminEmail = adminConfig.getString("email")
    val adminWebPass = adminConfig.getString("webPass")
    val adminAlarmPass = adminConfig.getString("alarmPass")
    val adminUser = User(None, "admin", adminEmail, List(), adminWebPass, adminAlarmPass, isAdmin = true)
    userDao.save(adminUser)
  }
}