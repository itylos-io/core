package com.itylos.core.exception

import spray.http.{StatusCode, StatusCodes}

/**
 * Exception that should be thrown when entered password is false
 */
class FalsePasswordException() extends CustomException {
  override var message: String = "False password!"
  override var code: Integer = 4011
  override var statusCode: StatusCode = StatusCodes.Unauthorized
}
