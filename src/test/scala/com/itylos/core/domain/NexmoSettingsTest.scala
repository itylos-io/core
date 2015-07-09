package com.itylos.core.domain

import com.itylos.core.exception.ParameterMissingException
import org.joda.time.DateTimeUtils
import org.json4s.JsonAST._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.domain.NexmoSettings]]
 */
class NexmoSettingsTest extends WordSpecLike with Matchers with BeforeAndAfterEach with ScalatestRouteTest {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Expected json fields
  val mobilesToNotify = JField("mobilesToNotify", new JArray(List(new JString("mobile"))))
  val nexmoKey = JField("nexmoKey", new JString("nexmoKey"))
  val nexmoSecret = JField("nexmoSecret", new JString("nexmoSecret"))
  val isEnabled = JField("isEnabled", new JBool(false))


  "A NexmoSettings" must {
    "throw exception when isEnabled is missing" in {
      val jObject = JObject(List(nexmoSecret, nexmoKey, mobilesToNotify))
      val settings = new NexmoSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when nexmoSecret is missing" in {
      val jObject = JObject(List(isEnabled, nexmoKey, mobilesToNotify))
      val settings = new NexmoSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when nexmoKey is missing" in {
      val jObject = JObject(List(isEnabled, nexmoSecret, mobilesToNotify))
      val settings = new NexmoSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when mobilesToNotify is missing" in {
      val jObject = JObject(List(isEnabled, nexmoSecret, nexmoKey))
      val settings = new NexmoSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "setup settings from json data" in {
      val jObject = JObject(List(isEnabled, nexmoSecret, nexmoKey, mobilesToNotify))
      val settings = new NexmoSettings()
      settings.fromJObject(jObject)
      settings.isEnabled shouldBe false
      settings.nexmoSecret shouldBe "nexmoSecret"
      settings.nexmoKey shouldBe "nexmoKey"
      settings.mobilesToNotify shouldBe List("mobile")
    }


  }

}
