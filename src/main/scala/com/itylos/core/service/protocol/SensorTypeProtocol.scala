package com.itylos.core.service.protocol

import com.itylos.core.domain.SensorType


/**
 * Describes the messages needed for sensor type management
 */
sealed trait SensorTypeProtocol extends Protocol

/**
 * Message to load sensor types
 */
case class LoadSensorTypes() extends SensorTypeProtocol


/**
 * Message to list all sensor types
 */
case class GetAllSensorTypesRq() extends SensorTypeProtocol

/**
 * Response message to GetAllSensorTypesRq
 */
case class GetAllSensorTypesRs(types:List[SensorType]) extends SensorTypeProtocol
