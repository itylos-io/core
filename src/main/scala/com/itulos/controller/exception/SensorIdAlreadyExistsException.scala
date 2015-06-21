package com.itulos.controller.exception

import spray.http.{StatusCodes, StatusCode}

/**
 * Exception that should be thrown when trying to create sensor with sensor id that already exists
 */
class SensorIdAlreadyExistsException(sensorId:String) extends CustomException {
  override var message: String = s"Sensor with id [$sensorId] already exists"
  override var code: Integer = 4005
  override var statusCode: StatusCode = StatusCodes.BadRequest
}
