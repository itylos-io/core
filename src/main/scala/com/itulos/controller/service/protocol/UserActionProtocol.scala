package com.itulos.controller.service.protocol

import com.itulos.controller.domain.{User, UserActionType}
import org.joda.time.DateTime


/**
 * Describes the messages needed for communication to manage user actions
 */
sealed trait UserActionProtocol extends Protocol

/**
 * Message to generate token
 * @param user the user to generate token for
 * @param action the UserActionType
 * @param date the date of the event
 * @param ip user's ip
 */
case class LogUserAction(user: User, action: UserActionType, date: Long = new DateTime().getMillis, ip: String) extends UserActionProtocol


