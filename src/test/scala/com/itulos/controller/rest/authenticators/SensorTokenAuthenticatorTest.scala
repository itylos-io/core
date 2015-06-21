package com.itulos.controller.rest.authenticators

import com.itulos.controller.dao.TestEnvironmentRepos
import com.itulos.controller.domain.SensorToken
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing.{AuthenticationFailedRejection, Directives}
import spray.testkit.ScalatestRouteTest

class SensorTokenAuthenticatorTest extends WordSpecLike with Matchers with TestEnvironmentRepos with BeforeAndAfterEach
with SensorEventsAuthenticator with Directives with ScalatestRouteTest {

  override val sensorTokenDao = TestEnvironmentRepos.sensorTokenDao.asInstanceOf[SensorTokenDao]

  override def beforeEach(): Unit = {
    reset(sensorTokenDao)
  }

  // Simple route to test
  val simpleRoute =
    get {
      pathSingleSlash {
        authenticate(sensorTokenAuthenticator) { data =>
          complete("OK")
        }
      }
    }

  "A SensorEventsAuthenticator" must {
    "fail with CredentialsMissing exception when sensorToken parameter is not present " in {
      Get() ~> simpleRoute ~> check {
        rejection === List(AuthenticationFailedRejection(CredentialsMissing, List()))
      }
    }
    "fail with CredentialsRejected exception when sensorToken is invalid " in {
      val tokenData = SensorToken(Some("userId"), "tokenStr", 100L)
      when(sensorTokenDao.getToken).thenReturn(Some(tokenData))
      Get("/?sensorToken=invalidTokenStr") ~> simpleRoute ~> check {
        rejection === List(AuthenticationFailedRejection(CredentialsRejected, List()))
      }
    }
    "should authenticate sensor when token is valid " in {
      val tokenData = SensorToken(Some("userId"), "tokenStr", 100L)
      when(sensorTokenDao.getToken).thenReturn(Some(tokenData))
      Get("/?sensorToken=tokenStr") ~> simpleRoute ~> check {
        responseAs[String] === "OK"
      }
    }

  }


}
