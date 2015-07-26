package com.itylos.core.rest.dto

import com.itylos.core.domain.{Sensor, SensorEvent}
import org.joda.time.{DateTime, DateTimeZone}

/**
 * DTO for SensorEvent
 */
case class SensorEventDto(var oid: String,
                          var sensorId: String,
                          var sensorName: String,
                          var sensorLocation: String,
                          var sensorTypeId: String,
                          var status: Int,
                          var batteryLevel: Int,
                          var dateOfEvent: Long,
                          var dateOfEventH: String) {

  /**
   * Constructor with a SensorEvent and a Sensor
   * @param sensorEvent the SensorEvent to get data from
   * @param sensor the Sensor to get data from
   */
  def this(sensorEvent: SensorEvent, sensor: Sensor) {
    this(
      if (sensorEvent.oid == None) "" else sensorEvent.oid.get,
      sensorEvent.sensorId,
      sensor.name,
      sensor.location,
      sensor.sensorTypeId,
      sensorEvent.status,
      sensorEvent.batteryLevel,
      sensorEvent.dateOfEvent,
      new DateTime().withMillis(sensorEvent.dateOfEvent).withZone(DateTimeZone.UTC).toString
    )
  }

}
