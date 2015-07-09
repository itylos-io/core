package com.itylos.core.exception

import spray.http.{StatusCodes, StatusCode}

/**
 * Exception that should be thrown when trying to associate with a non existing zone
 */
class ZoneDoesNotExistException(zoneOId:String) extends CustomException {
  override var message: String = s"Cannot associate with zone with oid [$zoneOId]. Probably zone does not exist"
  override var code: Integer = 4007
  override var statusCode: StatusCode = StatusCodes.Forbidden
}
