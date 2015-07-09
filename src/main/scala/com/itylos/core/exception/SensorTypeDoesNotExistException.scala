package com.itylos.core.exception

import spray.http.{StatusCodes, StatusCode}

/**
 * Exception that should be thrown when sensor type id does not correspond to an existing type
 */
class SensorTypeDoesNotExistException(oid:String) extends CustomException {
  override var message: String = s"Sensor type with id [$oid] does not exist"
  override var code: Integer = 4009
  override var statusCode: StatusCode = StatusCodes.BadRequest
}
