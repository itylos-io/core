package com.itulos.controller.exception

import spray.http.{StatusCode, StatusCodes}

/**
 * Exception that should be thrown when the token's user and the logged in user are not the same
 */
class TryToLogOutAnotherUserException() extends CustomException {
  override var message: String = "Try to logout another user exception. Token is not yours!"
  override var code: Integer = 4001
  override var statusCode: StatusCode = StatusCodes.Forbidden
}
