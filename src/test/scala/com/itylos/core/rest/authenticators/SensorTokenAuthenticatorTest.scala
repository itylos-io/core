package com.itylos.core.rest.authenticators

import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain.Settings
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing.{AuthenticationFailedRejection, Directives}
import spray.testkit.ScalatestRouteTest

class SensorTokenAuthenticatorTest extends WordSpecLike with Matchers with TestEnvironmentRepos with BeforeAndAfterEach
with AccessTokenAuthenticator with Directives with ScalatestRouteTest {

  override val settingsDao = TestEnvironmentRepos.settingsDao.asInstanceOf[SettingsDao]

  override def beforeEach(): Unit = {

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
      when(settingsDao.getSettings).thenReturn(Some(new Settings()))
      Get("/?sensorToken=invalidTokenStr") ~> simpleRoute ~> check {
        rejection === List(AuthenticationFailedRejection(CredentialsRejected, List()))
      }
    }
    "should authenticate sensor when token is valid " in {
      when(settingsDao.getSettings).thenReturn(Some(new Settings()))
      Get("/?sensorToken=accessToken_ChangeMe") ~> simpleRoute ~> check {
        responseAs[String] === "OK"
      }
    }
  }


}

