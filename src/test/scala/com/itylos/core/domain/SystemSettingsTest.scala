package com.itylos.core.domain

import com.itylos.core.exception.ParameterMissingException
import org.joda.time.DateTimeUtils
import org.json4s.JsonAST._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.domain.SystemSettings]]
 */
class SystemSettingsTest extends WordSpecLike with Matchers with BeforeAndAfterEach with ScalatestRouteTest {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Expected json fields
  val maxAlarmPasswordRetries = JField("maxAlarmPasswordRetries", new JInt(10))
  val maxSecondsToDisarm = JField("maxSecondsToDisarm", new JInt(9))
  val delayToArm = JField("delayToArm", new JInt(9))
  val accessToken = JField("accessToken", new JString("accessToken"))
  val playSoundsForSensorEvents = JField("playSoundsForSensorEvents", new JBool(false))
  val playSoundsForTriggeredAlarm = JField("playSoundsForTriggeredAlarm", new JBool(true))
  val playSoundsForAlarmStatusUpdates = JField("playSoundsForAlarmStatusUpdates", new JBool(false))

  "A SystemSettings" must {
    "throw exception when maxAlarmPasswordRetries is missing" in {
      val jObject = JObject(List(maxSecondsToDisarm, delayToArm, accessToken,
        playSoundsForAlarmStatusUpdates, playSoundsForSensorEvents, playSoundsForTriggeredAlarm))
      val settings = new SystemSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when maxSecondsToDisarm is missing" in {
      val jObject = JObject(List(maxAlarmPasswordRetries, delayToArm, accessToken,
        playSoundsForAlarmStatusUpdates, playSoundsForSensorEvents, playSoundsForTriggeredAlarm))
      val settings = new SystemSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when delayToArm is missing" in {
      val jObject = JObject(List(maxAlarmPasswordRetries, maxSecondsToDisarm, accessToken,
        playSoundsForAlarmStatusUpdates, playSoundsForSensorEvents, playSoundsForTriggeredAlarm))
      val settings = new SystemSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when accessToken is missing" in {
      val jObject = JObject(List(maxAlarmPasswordRetries, maxSecondsToDisarm, delayToArm,
        playSoundsForAlarmStatusUpdates, playSoundsForSensorEvents, playSoundsForTriggeredAlarm))
      val settings = new SystemSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when playSoundsForAlarmStatusUpdates is missing" in {
      val jObject = JObject(List(maxAlarmPasswordRetries, maxSecondsToDisarm, delayToArm, accessToken,
        playSoundsForSensorEvents, playSoundsForTriggeredAlarm))
      val settings = new SystemSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when playSoundsForSensorEvents is missing" in {
      val jObject = JObject(List(maxAlarmPasswordRetries, maxSecondsToDisarm, delayToArm, accessToken,
        playSoundsForAlarmStatusUpdates, playSoundsForTriggeredAlarm))
      val settings = new SystemSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when playSoundsForTriggeredAlarm is missing" in {
      val jObject = JObject(List(maxAlarmPasswordRetries, maxSecondsToDisarm, delayToArm, accessToken,
        playSoundsForAlarmStatusUpdates, playSoundsForSensorEvents))
      val settings = new SystemSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "setup object from json" in {
      val jObject = JObject(List(maxAlarmPasswordRetries, maxSecondsToDisarm, delayToArm, accessToken,
        playSoundsForAlarmStatusUpdates, playSoundsForSensorEvents, playSoundsForTriggeredAlarm))
      val settings = new SystemSettings()
      settings.fromJObject(jObject)
      settings.maxAlarmPasswordRetries shouldBe 10
      settings.accessToken shouldBe "accessToken"
      settings.delayToArm shouldBe 9
      settings.playSoundsForAlarmStatusUpdates shouldBe false
      settings.playSoundsForSensorEvents shouldBe false
      settings.playSoundsForTriggeredAlarm shouldBe true
      settings.maxSecondsToDisarm shouldBe 9
    }

  }

}
