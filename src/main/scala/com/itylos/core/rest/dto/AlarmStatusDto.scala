package com.itylos.core.rest.dto

import com.itylos.core.domain.{AlarmStatus, User}

/**
 * DTO for AlarmStatus
 */
case class AlarmStatusDto(currentStatus: String, user: Option[UserDto] = None) {


  /**
   * Constructor with a AlarmStatus
   */
  def this(alarmStatus: AlarmStatus, userData: User) {
    this(
      alarmStatus.status.toString,
      Some(new UserDto(userData))
    )
    user.get.alarmPassword = ""
    user.get.webPassword = ""
  }

}
