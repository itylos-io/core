package com.itylos.core.service.protocol

import com.itylos.core.domain.{KerberosInstance, Sensor}
import com.itylos.core.rest.dto.SensorDto


/**
 * Describes the messages needed for sensor data management
 */
sealed trait SensorProtocol extends Protocol

/**
 * Message to update new sensor
 */
case class UpdateSensorRq(sensor:Sensor) extends SensorProtocol

/**
 * Message to delete a sensor
 * @param id the object id of the sensor
 */
case class DeleteSensorRq(id:String) extends SensorProtocol

/**
 * Message to register new sensor
 */
case class CreateSensorRq(sensor:Sensor) extends SensorProtocol

/**
 * Message to update kerberos sensors
 * @param kerberosInstances the configures kerberos devices
 */
case class UpdateKerberosSensors(kerberosInstances:List[KerberosInstance]) extends SensorProtocol

/**
 * Response message for UpdateSensorRs and GetAllSensorsRq
 */
case class GetAllSensorRs(sensors:List[SensorDto]) extends SensorProtocol

/**
 * Message to get all sensors
 */
case class GetAllSensorsRq() extends SensorProtocol


