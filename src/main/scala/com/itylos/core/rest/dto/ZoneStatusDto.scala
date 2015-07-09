package com.itylos.core.rest.dto

import com.itylos.core.domain.{User, Zone, ZoneStatus}
import org.joda.time.{DateTime, DateTimeZone}

/**
 * DTO for ZoneStatus
 */
case class ZoneStatusDto(zoneId: String,
                         status: String,
                         zoneName: String,
                         dateUpdated: Long,
                         dateUpdatedH: String) {

  /**
   * Constructor with a AlarmStatus and Zone
   * @param zoneStatus the AlarmStatus to get data from
   * @param zone the Zone to get data from
   */
  def this(zoneStatus: ZoneStatus, zone: Zone) {
    this(
      zoneStatus.zoneId,
      zoneStatus.status.toString,
      zone.name,
      zoneStatus.dateUpdated,
      new DateTime().withMillis(zoneStatus.dateUpdated).withZone(DateTimeZone.UTC).toString
    )
  }

}
