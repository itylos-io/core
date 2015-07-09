package com.itylos.core.rest

import spray.http.HttpHeaders._
import spray.http.HttpMethods._
import spray.http._
import spray.routing._

trait CORSSupport {
  this: HttpService =>

  private val optionsCorsHeaders = List(
    `Access-Control-Allow-Headers`("Authorization,Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent"),
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Max-Age`(1000 * 60 * 60))

  protected def allowOriginHeader(ctx: RequestContext): HttpHeader = {
    val headers = ctx.request.headers.filter(h => h.is("origin"))
    if (headers.nonEmpty)
      `Access-Control-Allow-Origin`(SomeOrigins(List(headers.head.value)))
    else
      `Access-Control-Allow-Origin`(AllOrigins)
  }

  def cors[T]: Directive0 = mapRequestContext { ctx => ctx.withRouteResponseHandling({

    //It is an option request for a resource that responds to some other method
    case Rejected(x) if (ctx.request.method.equals(HttpMethods.OPTIONS)) => {
      val allowedMethods: List[HttpMethod] = x.filter(_.isInstanceOf[MethodRejection]).map(rejection => {
        rejection.asInstanceOf[MethodRejection].supported
      })

      val moreAllowedMethods :List[HttpMethod] = allowedMethods.++:(List(DELETE,PUT))

      ctx.complete(HttpResponse().withHeaders(
        `Access-Control-Allow-Methods`(OPTIONS,moreAllowedMethods: _*) :: allowOriginHeader(ctx) ::
          optionsCorsHeaders
      ))
    }
  }).withHttpResponseHeadersMapped { headers =>
    allowOriginHeader(ctx) :: headers
  }
  }


}