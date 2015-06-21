package com.itulos.controller.rest.dto

import com.itulos.controller.domain.{Zone, ZoneStatus}
import org.joda.time.{DateTime, DateTimeZone}

/**
 * DTO for Zone
 */
case class ZoneDto(oid: String,
                   name: String,
                   description: String,
                   sensors: Option[List[SensorDto]],
                   status: String,
                   dateRegistered: Long,
                   dateRegisteredH: String) {
  /**
   * Constructor with a Zone and a list of Sensors
   * @param zone the Zone to get data from
   * @param zoneStatus the status of the zone
   * @param sensors the sensor DTOs
   */
  def this(zone: Zone, zoneStatus: ZoneStatus, sensors: Option[List[SensorDto]]) {
    this(
      zone.oid.get,
      zone.name,
      zone.description,
      sensors,
      zoneStatus.status.toString,
      zone.dateCreated,
      new DateTime().withMillis(zone.dateCreated).withZone(DateTimeZone.UTC).toString
    )
  }

}
