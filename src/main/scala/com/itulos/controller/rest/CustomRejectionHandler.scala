package com.itulos.controller.rest

import spray.http.HttpHeaders.Allow
import spray.http.StatusCodes
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}
import spray.routing.{MethodRejection, AuthenticationFailedRejection, RejectionHandler, HttpService}


trait CustomRejectionHandler  extends HttpService with CORSSupport{

    implicit val customRejectionHandler = RejectionHandler {

      case AuthenticationFailedRejection(cause, challengeHeaders) :: _ ⇒
        val rejectionMessage = cause match {
          case CredentialsMissing  ⇒ "The resource requires authentication, which was not supplied with the request"
          case CredentialsRejected ⇒ "The supplied authentication is invalid"
        }
      { ctx ⇒ ctx.complete(StatusCodes.Unauthorized, challengeHeaders.::(allowOriginHeader(ctx)), rejectionMessage) }

      case rejections @ (MethodRejection(_) :: _) ⇒
        val methods = rejections.collect { case MethodRejection(method) ⇒ method }
        complete(StatusCodes.MethodNotAllowed, List(Allow(methods: _*)), "HTTP method not allowed, supported methods: " + methods.mkString(", "))

    }

}
