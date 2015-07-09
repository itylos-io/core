package com.itylos.core.service.protocol

import com.itylos.core.domain.User
import com.itylos.core.rest.dto.UserDto

/**
 * Describes the messages needed for communication to manage users data
 */
sealed trait UserProtocol extends Protocol

/**
 * Message to load admin user
 */
case class LoadAdminUser() extends UserProtocol

/**
 * Message to create new user
 * @param user the new User
 */
case class CreateUserRq(user: User) extends UserProtocol

/**
 * Message to update user
 * @param user the updated User
 */
case class UpdateUserRq(user: User) extends UserProtocol

/**
 * Message to delete an existing user
 * @param userId the id of the user to delete
 */
case class DeleteUserRq(userId: String) extends UserProtocol

/**
 * Response Message to CreateUserRq
 * @param user the new UserDto
 */
case class CreateUserRs(user: UserDto) extends UserProtocol

/**
 * Message to request users data
 */
case class GetUsersRq() extends UserProtocol

/**
 * Message to request user data
 */
case class GetUserRq(userId:String) extends UserProtocol

/**
 * Response message to GetUserRq
 * @param user the userDto
 */
case class GetUserRs(user: UserDto) extends UserProtocol

/**
 * Response message to GetUsersRq
 * @param users the userDto
 */
case class GetUsersRs(users: List[UserDto]) extends UserProtocol


