package com.itulos.controller.exception

import spray.http.{StatusCodes, StatusCode}

/**
 * Exception that should be thrown when object id is invalid. Not parsable
 */
class InvalidObjectIdException(oid:String) extends CustomException {
  override var message: String = s"Cannot parse object id [$oid]. Probably invalid"
  override var code: Integer = 4008
  override var statusCode: StatusCode = StatusCodes.Forbidden
}
