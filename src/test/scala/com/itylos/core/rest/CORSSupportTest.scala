package com.itylos.core.rest

import com.itylos.core.dao.TestEnvironmentRepos
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.http.HttpHeaders.RawHeader
import spray.routing.{Directives, HttpService}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.rest.CORSSupport]]
 */
class CORSSupportTest extends WordSpecLike with Matchers with TestEnvironmentRepos with BeforeAndAfterEach
with CORSSupport with HttpService with Directives with ScalatestRouteTest {
  def actorRefFactory = system // Connect the service API to the test ActorSystem


  // Simple route to test
  val simpleRoute = pathSingleSlash {
    cors {
      delete {
        complete((200, "'CORS it works!"))
      } ~
        get {
          complete((200, "'CORS it works!"))
        } ~
        post {
          complete((200, "'CORS I'll update that!"))
        }
    }
  }


  "A GET response" must {
    "contain Access-Control-Allow-Origin header for GET request" in {
      val originHeader = RawHeader("Origin", "http://itylos.io")
      Get() ~> originHeader ~> simpleRoute ~> check {
        responseAs[String] should be("'CORS it works!")
        header("Access-Control-Allow-Origin").get.value should be("http://itylos.io")
      }
    }
    "contain Access-Control-Allow-Origin header for POST request" in {
      val originHeader = RawHeader("Origin", "http://itylos.io")
      Post() ~> originHeader ~> simpleRoute ~> check {
        responseAs[String] should be("'CORS I'll update that!")
        header("Access-Control-Allow-Origin").get.value should be("http://itylos.io")
      }
    }
    "contain all preflight request headers for OPTIONS" in {
      val originHeader = RawHeader("Origin", "http://itylos.io")
      Options() ~> originHeader ~> simpleRoute ~> check {
        status.intValue should be(200)
        header("Access-Control-Allow-Credentials").get.value should be("true")
        header("Access-Control-Max-Age").get.value should be("" + 1000 * 60 * 60)

        val allowMethods = header("Access-Control-Allow-Methods").get.value.split(", ")
        Array("OPTIONS", "POST", "GET", "DELETE", "PUT") foreach {
          allowMethods should contain(_)
        }
      }
    }


  }


}
