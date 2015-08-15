package com.itylos.core.rest.dto

import com.itylos.core.domain.{Sensor, SensorEventsStatistics}

/**
 * DTO for [[com.itylos.core.domain.SensorEventsStatistics]]
 */
case class SensorEventsStatisticsDto(sensorId: String,
                                     sensorName: String,
                                     sensorEventsCount: Int,
                                     datetimeInterval: Long
                                      ) {

  /**
   * Constructor with a Sensor and SensorEventsStatistics
   */
  def this(sensor: Sensor, sensorStatistics: SensorEventsStatistics) {
    this(
      sensor.sensorId,
      sensor.name,
      sensorStatistics.sensorEventsCount,
      sensorStatistics.datetimeInterval
    )
  }

}
