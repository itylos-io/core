package com.itylos.core.domain

import com.itylos.core.exception.ParameterMissingException
import org.joda.time.DateTimeUtils
import org.json4s.JsonAST._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.domain.User]]
 */
class UserTest extends WordSpecLike with Matchers with BeforeAndAfterEach with ScalatestRouteTest {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Expected json fields
  val userOId = JField("oid", new JString("oid"))
  val userName = JField("name", new JString("name"))
  val userEmail = JField("email", new JString("email"))
  val userWebPass = JField("webPassword", new JString("webPassword"))
  val userAlarmPass = JField("alarmPassword", new JString("alarmPassword"))
  val userIdAdmin = JField("isAdmin", new JBool(false))


  "A User" must {
    "throw exception when user id is missing and it's required" in {
      val jObject = JObject(List(userName, userEmail, userWebPass, userAlarmPass, userIdAdmin))
      val user = new User()
      intercept[ParameterMissingException] {
        user.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when user name is missing " in {
      val jObject = JObject(List(userOId, userEmail, userWebPass, userAlarmPass, userIdAdmin))
      val user = new User()
      intercept[ParameterMissingException] {
        user.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when user email is missing" in {
      val jObject = JObject(List(userOId, userName, userWebPass, userAlarmPass, userIdAdmin))
      val user = new User()
      intercept[ParameterMissingException] {
        user.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when user web pass is missing" in {
      val jObject = JObject(List(userOId, userName, userEmail, userAlarmPass, userIdAdmin))
      val user = new User()
      intercept[ParameterMissingException] {
        user.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when user alarm pass is missing" in {
      val jObject = JObject(List(userOId, userName, userEmail, userWebPass, userIdAdmin))
      val user = new User()
      intercept[ParameterMissingException] {
        user.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when user is admin is missing" in {
      val jObject = JObject(List(userOId, userName, userEmail, userWebPass, userAlarmPass))
      val user = new User()
      intercept[ParameterMissingException] {
        user.fromJObject(jObject, isIdRequired = true)
      }
    }
    "setup user from json data" in {
      val jObject = JObject(List(userOId, userName, userEmail, userWebPass, userAlarmPass, userIdAdmin))
      val user = new User()
      user.fromJObject(jObject, isIdRequired = true)
      user.oid shouldBe Some("oid")
      user.name shouldBe "name"
      user.email shouldBe "email"
      user.webPassword shouldBe "webPassword"
      user.alarmPassword shouldBe "alarmPassword"
      user.isAdmin shouldBe false
    }


  }

}
