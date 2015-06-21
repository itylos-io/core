package com.itulos.controller.rest

import com.itulos.controller.exception.CustomException
import com.itulos.controller.rest.dto.{Metadata, RootResponse}
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