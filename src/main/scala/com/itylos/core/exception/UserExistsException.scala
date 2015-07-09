package com.itylos.core.exception

import spray.http.{StatusCode, StatusCodes}

/**
 * Exception that should be thrown when try to create a user that already exists
 * @param email the name of the parameter
 */
class UserExistsException(email: String) extends CustomException {
  override var message: String = s"User with email [$email] exists"
  override var code: Integer = 4004
  override var statusCode: StatusCode = StatusCodes.BadRequest
}
