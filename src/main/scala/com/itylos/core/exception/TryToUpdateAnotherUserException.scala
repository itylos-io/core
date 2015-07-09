package com.itylos.core.exception

import spray.http.{StatusCode, StatusCodes}

/**
 * Exception that should be thrown when the token's user and the logged in user are not the same
 */
class TryToUpdateAnotherUserException() extends CustomException {
  override var message: String = "Try to update another user's token. Token is not yours!"
  override var code: Integer = 4003
  override var statusCode: StatusCode = StatusCodes.Forbidden
}
