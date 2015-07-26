package com.itylos.core.rest.dto

import com.itylos.core.domain.{AlarmStatusHistory, User}
import com.itylos.core.exception.DISARMED
import org.joda.time.{DateTime, DateTimeZone}

/**
 * DTO for AlarmStatusHistory
 */
case class AlarmStatusHistoryDto(var status: String = "",
                                 var timeOfStatusUpdate: Long,
                                 var timeOfStatusUpdateH: String,
                                 var user: UserDto
                                  ) {


  /**
   * Constructor with a AlarmStatus and a User
   */
  def this(alarmStatus: AlarmStatusHistory, userPerformedAction: User) {
    this( alarmStatus.status.toString,0L,"", new UserDto(userPerformedAction))

    if (alarmStatus.status == DISARMED) {
      timeOfStatusUpdate = alarmStatus.timeDisArmed
      timeOfStatusUpdateH = new DateTime().withMillis(alarmStatus.timeDisArmed).withZone(DateTimeZone.UTC).toString
    } else {
      timeOfStatusUpdate = alarmStatus.timeArmed
      timeOfStatusUpdateH = new DateTime().withMillis(alarmStatus.timeArmed).withZone(DateTimeZone.UTC).toString
    }

  }

}
