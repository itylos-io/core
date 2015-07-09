package com.itylos.core.exception

import spray.http.{StatusCode, StatusCodes}

/**
 * Exception that should be thrown when trying to arm system and no zone is ENABLED
 */
class CannotArmWithNoEnabledZone() extends CustomException {
  override var message: String = s"Cannot arm system since there is no enabled zone."
  override var code: Integer = 4010
  override var statusCode: StatusCode = StatusCodes.BadRequest
}
