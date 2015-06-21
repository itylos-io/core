package com.itulos.controller.rest.dto

import com.itulos.controller.domain.{User, AlarmStatus}

/**
 * DTO for AlarmStatus
 */
case class AlarmStatusDto(currentStatus: String, user: Option[UserDto]=None) {


  /**
   * Constructor with a AlarmStatus
   */
  def this(alarmStatus: AlarmStatus, user: User) {
    this(
      alarmStatus.status.toString,
      Some(new UserDto(user))
    )
  }

}
