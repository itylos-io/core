package com.itulos.controller.service.protocol

import com.itulos.controller.domain.SensorToken
import com.itulos.controller.rest.dto.SensorTokenDto


/**
 * Describes the messages needed for sensor token management
 */
sealed trait SensorTokenProtocol extends Protocol

/**
 * Message to update SensorToken
 * @param token the new token
 * @param forceUpdate if true it will remove the old and update else if token exist will not update
 */
case class UpdateSensorToken(token: SensorToken, forceUpdate: Boolean=false) extends SensorTokenProtocol

/**
 * Message to get SensorToken
 */
case class GetSensorTokenRq() extends SensorTokenProtocol

/**
 * Response message to GetSensorTokenRq
 */
case class GetSensorTokenRs(sensorToken: SensorTokenDto) extends SensorTokenProtocol






