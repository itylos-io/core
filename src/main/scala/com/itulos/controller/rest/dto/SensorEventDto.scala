package com.itulos.controller.rest.dto

import com.itulos.controller.domain.{Sensor, SensorEvent}
import org.joda.time.{DateTime, DateTimeZone}

/**
 * DTO for SensorEvent
 */
case class SensorEventDto(var oid: String,
                          var sensorId: String,
                          var sensorName: String,
                          var sensorLocation: String,
                          var status: String,
                          var dateOfEvent: Long,
                          var dateOfEventH: String) {

  /**
   * Constructor with a SensorEvent and a Sensor
   * @param sensorEvent the SensorEvent to get data from
   * @param sensor the Sensor to get data from
   */
  def this(sensorEvent: SensorEvent, sensor: Sensor) {
    this(
      sensorEvent.oid.get,
      sensorEvent.sensorId,
      sensor.name,
      sensor.location,
      sensorEvent.status.toString,
      sensorEvent.dateOfEvent,
      new DateTime().withMillis(sensorEvent.dateOfEvent).withZone(DateTimeZone.UTC).toString
    )
  }

}
