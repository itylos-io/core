package com.itylos.core.exception

import spray.http.StatusCode

/**
 * Custom exception which holds a message and a specific code ID for every type of exception
 */
trait CustomException extends RuntimeException {
  var message: String
  var code: Integer
  var statusCode: StatusCode
}

