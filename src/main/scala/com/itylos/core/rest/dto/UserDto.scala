package com.itylos.core.rest.dto

import com.itylos.core.domain.User
import org.joda.time.{DateTime, DateTimeZone}

/**
 * DTO for User
 */
case class UserDto(
                    var oid: String,
                    var userId: String,
                    var name: String,
                    var email: String,
                    var webPassword: String,
                    var alarmPassword: String,
                    var phones: List[String],
                    var dateRegistered: Long,
                    var dateRegisteredH: String,
                    var isAdmin: Boolean) {
  /**
   * Constructor with a User
   * @param user the User to get data from
   */
  def this(user: User) {
    this(
      user.oid.getOrElse("-999"),
      user.oid.getOrElse("-999"),
      user.name,
      user.email,
      user.webPassword,
      user.alarmPassword,
      user.phones,
      user.dateRegistered,
      new DateTime().withMillis(user.dateRegistered).withZone(DateTimeZone.UTC).toString,
      user.isAdmin
    )
  }

}
