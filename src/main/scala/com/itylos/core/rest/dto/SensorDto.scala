package com.itylos.core.rest.dto

import com.itylos.core.domain.{Sensor, SensorType}
import org.joda.time.{DateTime, DateTimeZone}

/**
 * DTO for User
 */
case class SensorDto(oid: String,
                     sensorId: String,
                     name: String,
                     description: String,
                     location: String,
                     sensorTypeId: String,
                     sensorTypeName: String,
                     isActive: Boolean = true,
                     zones: Option[List[ZoneDto]],
                     dateRegistered: Long,
                     dateRegisteredH: String) {

  /**
   * Constructor with a Sensor and SensorType
   * @param sensor the Sensor to get data from
   * @param sensorType the SensorType to get data from
   * @param zones the zones associated to sensor if any
   */
  def this(sensor: Sensor, sensorType: SensorType, zones: Option[List[ZoneDto]]) {
    this(
      sensor.oid.get,
      sensor.sensorId,
      sensor.name,
      sensor.description,
      sensor.location,
      sensor.sensorTypeId,
      sensorType.name,
      sensor.isActive,
      zones,
      sensor.dateRegistered,
      new DateTime().withMillis(sensor.dateRegistered).withZone(DateTimeZone.UTC).toString
    )
  }

}
