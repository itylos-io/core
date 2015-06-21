package com.itulos.controller.exception

import spray.http.{StatusCode, StatusCodes}

/**
 * Exception that should be thrown when trying to associate with a non existing sensor id
 */
class SensorDoesNotExistException(sensorId:String) extends CustomException {
  override var message: String = s"Cannot associate with sensor with id [$sensorId]. Probably sensor does not exist"
  override var code: Integer = 4006
  override var statusCode: StatusCode = StatusCodes.Forbidden
}
