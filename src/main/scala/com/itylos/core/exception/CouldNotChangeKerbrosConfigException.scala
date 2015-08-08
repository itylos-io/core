package com.itylos.core.exception

import spray.http.{StatusCode, StatusCodes}

/**
 * Exception that should be thrown when trying to change configuration for a kerberos instance and operation fails
 */
class CouldNotChangeKerbrosConfigException(instance:String) extends CustomException {
  override var message: String = s"Could not change configuration of kerberos instance [$instance]!"
  override var code: Integer = 4011
  override var statusCode: StatusCode = StatusCodes.BadRequest
}
