package com.itylos.core.rest

import com.itylos.core.exception.CustomException
import com.itylos.core.rest.dto.{Metadata, RootResponse}
import spray.http.MediaTypes
import spray.httpx.Json4sSupport
import spray.routing._

/**
 * Custom exception handler to format error responses according to API guidelines
 */
trait CustomExceptionHandler extends HttpService with Json4sSupport {
  implicit val customExceptionHandler = ExceptionHandler {
    case e: CustomException =>
      respondWithMediaType(MediaTypes.`application/json`) {
        complete(e.statusCode, RootResponse(Metadata(e.code, Some(e.message), None), None))
      }
  }
}