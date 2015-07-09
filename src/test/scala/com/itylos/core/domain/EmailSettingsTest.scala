package com.itylos.core.domain

import com.itylos.core.exception.ParameterMissingException
import org.joda.time.DateTimeUtils
import org.json4s.JsonAST._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.domain.EmailSettings]]
 */
class EmailSettingsTest extends WordSpecLike with Matchers with BeforeAndAfterEach with ScalatestRouteTest {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Expected json fields
 val isEnabled = JField("isEnabled", new JBool(false))
  val emailsToNotify = JField("emailsToNotify", new JArray(List(new JString("email"))))
  val smtpUser = JField("smtpUser", new JString("smtpUser"))
  val smtpPassword = JField("smtpPassword", new JString("smtpPassword"))
  val smtpPort = JField("smtpPort", new JInt(500))
  val smtpStartTLSEnabled = JField("smtpStartTLSEnabled", new JBool(false))
  val smtpAuth = JField("smtpAuth", new JBool(false))

  "A EmailSettings" must {
    "throw exception when isEnabled is missing" in {
      val jObject = JObject(List( emailsToNotify, smtpUser,smtpPassword,smtpPort,smtpStartTLSEnabled,smtpAuth))
      val settings = new EmailSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when emailsToNotify is missing" in {
      val jObject = JObject(List(isEnabled, smtpUser,smtpPassword,smtpPort,smtpStartTLSEnabled,smtpAuth))
      val settings = new EmailSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when smtpUser is missing" in {
      val jObject = JObject(List(isEnabled, emailsToNotify,smtpPassword,smtpPort,smtpStartTLSEnabled,smtpAuth))
      val settings = new EmailSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when smtpPassword is missing" in {
      val jObject = JObject(List(isEnabled, emailsToNotify, smtpUser,smtpPort,smtpStartTLSEnabled,smtpAuth))
      val settings = new EmailSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when smtpPort is missing" in {
      val jObject = JObject(List(isEnabled, emailsToNotify, smtpUser,smtpPassword,smtpStartTLSEnabled,smtpAuth))
      val settings = new EmailSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when smtpStartTLSEnabled is missing" in {
      val jObject = JObject(List(isEnabled, emailsToNotify, smtpUser,smtpPassword,smtpPort,smtpAuth))
      val settings = new EmailSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when smtpAuth is missing" in {
      val jObject = JObject(List(isEnabled, emailsToNotify, smtpUser,smtpPassword,smtpPort))
      val settings = new EmailSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "setup settings from json data" in {
      val jObject = JObject(List(isEnabled, emailsToNotify, smtpUser,smtpPassword,smtpPort,smtpStartTLSEnabled,smtpAuth))
      val settings = new EmailSettings()
      settings.fromJObject(jObject)
      settings.isEnabled shouldBe false
      settings.smtpUser shouldBe "smtpUser"
      settings.smtpPassword shouldBe "smtpPassword"
      settings.smtpPort shouldBe 500
      settings.smtpAuth shouldBe false
      settings.emailsToNotify shouldBe List("email")
    }
  }

}
