package com.itylos.core.domain

import com.itylos.core.exception.ParameterMissingException
import org.joda.time.DateTimeUtils
import org.json4s.JsonAST._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.domain.PushBulletSettings]]
 */
class PushBulletSettingsTest extends WordSpecLike with Matchers with BeforeAndAfterEach with ScalatestRouteTest {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Expected json fields
  val isEnabled = JField("isEnabled", new JBool(false))
  val notifyForSensorEvents = JField("notifyForSensorEvents", new JBool(false))
  val notifyForAlarms = JField("notifyForAlarms", new JBool(false))
  val notifyForAlarmsStatusUpdates = JField("notifyForAlarmsStatusUpdates", new JBool(true))
  val accessToken = JField("accessToken", new JString("accessToken"))

  val deviceEnabled = JField("isEnabled", new JBool(true))
  val deviceIden = JField("iden", new JString("iden"))
  val deviceName = JField("deviceName", new JString("deviceName"))
  val device = new JObject(List(deviceEnabled, deviceIden, deviceName))
  val devices = JField("devices", new JArray(List(device)))

  "A PushBulletSettings" must {
    "throw exception when isEnabled is missing" in {
      val jObject = JObject(List(notifyForAlarms, notifyForSensorEvents, notifyForAlarmsStatusUpdates, accessToken, devices))
      val settings = new PushBulletSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when notifyForAlarms is missing" in {
      val jObject = JObject(List(isEnabled, notifyForSensorEvents, notifyForAlarmsStatusUpdates, accessToken, devices))
      val settings = new PushBulletSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when notifyForSensorEvents is missing" in {
      val jObject = JObject(List(isEnabled, notifyForAlarms, notifyForAlarmsStatusUpdates, accessToken, devices))
      val settings = new PushBulletSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when notifyForAlarmsStatusUpdates is missing" in {
      val jObject = JObject(List(isEnabled, notifyForAlarms, notifyForSensorEvents, accessToken, devices))
      val settings = new PushBulletSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when accessToken is missing" in {
      val jObject = JObject(List(isEnabled, notifyForAlarms, notifyForSensorEvents, notifyForAlarmsStatusUpdates,
        devices))
      val settings = new PushBulletSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "throw exception when devices is missing" in {
      val jObject = JObject(List(isEnabled, notifyForAlarms, notifyForSensorEvents, notifyForAlarmsStatusUpdates,
        accessToken))
      val settings = new PushBulletSettings()
      intercept[ParameterMissingException] {
        settings.fromJObject(jObject)
      }
    }
    "setup object from json" in {
      val jObject = JObject(List(isEnabled, notifyForAlarms, notifyForSensorEvents, notifyForAlarmsStatusUpdates,
        accessToken, devices))
      val settings = new PushBulletSettings()
      settings.fromJObject(jObject)
      settings.isEnabled shouldBe false
      settings.accessToken shouldBe "accessToken"
      settings.notifyForAlarms shouldBe false
      settings.notifyForSensorEvents shouldBe false
      settings.notifyForAlarmsStatusUpdates shouldBe true
    }

  }

}
