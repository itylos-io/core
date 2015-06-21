package com.itulos.controller.exception

import spray.http.{StatusCode, StatusCodes}

/**
 * Exception that should be thrown when a parameter is missing from expected paylod
 * @param parameter the name of the parameter
 */
class ParameterMissingException(parameter: String) extends CustomException {
  override var message: String = s"Missing paramater [$parameter]"
  override var code: Integer = 4002
  override var statusCode: StatusCode = StatusCodes.BadRequest
}
