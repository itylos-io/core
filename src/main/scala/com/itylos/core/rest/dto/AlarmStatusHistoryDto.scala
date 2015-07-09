package com.itylos.core.rest.dto

import com.itylos.core.domain.{AlarmStatusHistory, User}
import com.itylos.core.exception.DISARMED
import org.joda.time.{DateTime, DateTimeZone}

/**
 * DTO for AlarmStatusHistory
 */
case class AlarmStatusHistoryDto(var status: String = "",
                                 var timeArmed: Option[Long] = None,
                                 var timeDisarmed: Option[Long] = None,
                                 var timeArmedH: Option[String] = None,
                                 var timeDisarmedH: Option[String] = None,
                                 var userArmed: Option[UserDto] = None,
                                 var userDisarmed: Option[UserDto] = None
                                  ) {


  /**
   * Constructor with a AlarmStatus and a User
   */
  def this(alarmStatus: AlarmStatusHistory, user: User) {
    this(status = alarmStatus.status.toString)
    val userDto = new UserDto(user)

    if (alarmStatus.status == DISARMED) {
      timeDisarmed = Some(alarmStatus.timeDisArmed)
      timeDisarmedH = Some(new DateTime().withMillis(alarmStatus.timeDisArmed).withZone(DateTimeZone.UTC).toString)
      userDisarmed = Some(userDto)
    } else {
      timeArmed = Some(alarmStatus.timeArmed)
      timeArmedH = Some(new DateTime().withMillis(alarmStatus.timeArmed).withZone(DateTimeZone.UTC).toString)
      userArmed = Some(userDto)
    }

  }

}
