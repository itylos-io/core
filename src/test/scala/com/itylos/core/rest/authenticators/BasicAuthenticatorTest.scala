package com.itylos.core.rest.authenticators

import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain.User
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.http.BasicHttpCredentials
import spray.http.HttpHeaders.Authorization
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing.{AuthenticationFailedRejection, Directives}
import spray.testkit.ScalatestRouteTest

class BasicAuthenticatorTest extends WordSpecLike with Matchers with TestEnvironmentRepos with BeforeAndAfterEach
with BasicAuthenticator with Directives with ScalatestRouteTest {

  override val userDao = TestEnvironmentRepos.userDao.asInstanceOf[UserDao]

  override def beforeEach(): Unit = {
    reset(userDao)
  }

  // Simple route to test
  val simpleRoute =
    get {
      pathSingleSlash {
        authenticate(basicUserAuthenticator) { user =>
          complete("email:" + user.user.email)
        }
      }
    }

  // User data to use
  val user = new User()
  user.email = "admin@itylos.io"
  user.oid = Some("userId")
  user.webPassword = "123456"


  "A BasicAuthenticator" must {
    "fail with CredentialsMissing exception when no Authorization header is present" in {
      Get() ~> simpleRoute ~> check {
        rejection === List(AuthenticationFailedRejection(CredentialsMissing, List()))
      }
    }
    "fail with CredentialsRejected exception when email does not exist" in {
      when(userDao.getUserByEmail("admin@itylos.io")).thenReturn(None)
      val invalidBasicAuth = Authorization(BasicHttpCredentials("admin@itylos.io", "12345"))
      Get() ~> invalidBasicAuth ~> simpleRoute ~> check {
        rejection === List(AuthenticationFailedRejection(CredentialsRejected, List()))
      }
    }
    "fail with CredentialsRejected exception when password is wrong" in {
      when(userDao.getUserByEmail("admin@itylos.io")).thenReturn(Some(user))
      val invalidBasicAuth = Authorization(BasicHttpCredentials("admin@itylos.io", "12345"))
      Get() ~> invalidBasicAuth ~> simpleRoute ~> check {
        rejection === List(AuthenticationFailedRejection(CredentialsRejected, List()))
      }
    }
    "authenticate user when all credentials are valid " in {
      when(userDao.getUserByEmail("admin@itulos.io")).thenReturn(Some(user))
      val validBasicAuth = Authorization(BasicHttpCredentials("admin@itylos.io", "123456"))
      Get() ~> validBasicAuth ~> simpleRoute ~> check {
        responseAs[String] === "email:admin@itylos.io"
      }
    }

  }


}
