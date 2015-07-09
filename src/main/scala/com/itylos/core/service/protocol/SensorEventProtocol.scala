package com.itylos.core.service.protocol

import com.itylos.core.domain.SensorEvent
import com.itylos.core.rest.dto.SensorEventDto


/**
  * Describes the messages needed for sensor events data management
  */
sealed trait SensorEventProtocol extends Protocol

/**
 * Message to add new sensor event
 */
case class AddSensorEventRq(sensorEvent:SensorEvent) extends SensorEventProtocol

/**
 * Message to get sensor event
 */
case class GetSensorEventsRq(sensorId:Option[String],limit:Int,offset:Int) extends SensorEventProtocol

/**
 * List sensor events
 */
case class GetSensorEventsRs(sensorEvents:List[SensorEventDto]) extends SensorEventProtocol

/**
 * List latest sensor event for each sensor
 */
case class GetSensorLatestEventsRq(sensorIds:List[String]) extends SensorEventProtocol

/**
 * Message to delete sensor events associated to a sensor
 * @param sensorOId the oid of the sensor
 */
case class RemoveSensorEventsForSensor(sensorOId:String) extends SensorEventProtocol