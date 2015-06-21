package com.itulos.controller.rest.authenticators

import com.itulos.controller.dao.TestEnvironmentRepos
import com.itulos.controller.domain.{User, Token}
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing.{AuthenticationFailedRejection, Directives}
import spray.testkit.ScalatestRouteTest

class TokenAuthenticatorTest extends WordSpecLike with Matchers with TestEnvironmentRepos with BeforeAndAfterEach
with TokenAuthenticator with Directives with ScalatestRouteTest {

  override val tokenDao = TestEnvironmentRepos.tokenDao.asInstanceOf[TokenDao]
  override val userDao = TestEnvironmentRepos.userDao.asInstanceOf[UserDao]

  override def beforeEach(): Unit = {
    reset(tokenDao)
    reset(userDao)
  }

  // Simple route to test
  val simpleRoute =
    get {
      pathSingleSlash {
        authenticate(tokenAuthenticator) { user =>
          complete("email:"+user.email)
        }
      }
    }


  "A TokenServiceActor" must {
    "fail with CredentialsMissing exception when token parameter is not present " in {
      Get() ~> simpleRoute ~> check {
        rejection === List(AuthenticationFailedRejection(CredentialsMissing, List()))
      }
    }
    "fail with CredentialsRejected exception when token has been removed " in {
      when(tokenDao.getToken("tokenStr")).thenReturn(None)
      Get("/?token=tokenStr") ~> simpleRoute ~> check {
        rejection === List(AuthenticationFailedRejection(CredentialsRejected, List()))
      }
    }
    "fail with CredentialsRejected exception when token has expired " in {
      val tokenData = Token("userId", "tokenStr", 1000L)
      when(tokenDao.getToken("tokenStr")).thenReturn(Some(tokenData))
      Get("/?token=tokenStr") ~> simpleRoute ~> check {
        rejection === List(AuthenticationFailedRejection(CredentialsRejected, List()))
      }
    }
    "authenticate user when all credentials are valid " in {
      val user = new User()
      user.email="admin@itulos.io"
      user.oid = Some("userId")
      val tokenData = Token("userId", "tokenStr", new DateTime().plusHours(1).getMillis)
      when(tokenDao.getToken("tokenStr")).thenReturn(Some(tokenData))
      when(userDao.getUserByObjectId("userId")).thenReturn(Some(user))
      Get("/?token=tokenStr") ~> simpleRoute ~> check {
        responseAs[String] === "email:admin@itulos.io"
      }
    }
  }


}
