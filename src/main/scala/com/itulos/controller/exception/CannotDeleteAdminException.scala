package com.itulos.controller.exception

import spray.http.{StatusCodes, StatusCode}

/**
 * Exception that should be thrown when trying to delete super admin (user id 1)
 */
class CannotDeleteAdminException() extends CustomException {
  override var message: String = s"Cannon delete super admin with id [1]"
  override var code: Integer = 4004
  override var statusCode: StatusCode = StatusCodes.Forbidden
}
