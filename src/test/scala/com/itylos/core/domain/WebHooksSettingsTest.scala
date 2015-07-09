package com.itylos.core.domain

import com.itylos.core.exception.ParameterMissingException
import org.joda.time.DateTimeUtils
import org.json4s.JsonAST._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.domain.WebHookSettings]]
 */
class WebHooksSettingsTest extends WordSpecLike with Matchers with BeforeAndAfterEach with ScalatestRouteTest {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Expected json fields
  val isEnabled = JField("isEnabled", new JBool(false))
  val uris = JField("uris", new JArray(List(new JString("fooUrl"))))


  "A WebHookSetting" must {
    "throw exception when is enabled is missing" in {
      val jObject = JObject(List(uris))
      val webHookSettings = new WebHookSettings()
      intercept[ParameterMissingException] {
        webHookSettings.fromJObject(jObject)
      }
    }
    "throw exception when uris are missing" in {
      val jObject = JObject(List(isEnabled))
      val webHookSettings = new WebHookSettings()
      intercept[ParameterMissingException] {
        webHookSettings.fromJObject(jObject)
      }
    }
    "setup webhook settings from json data" in {
      val jObject = JObject(List(isEnabled, uris))
      val webHookSettings = new WebHookSettings()
      webHookSettings.fromJObject(jObject)
      webHookSettings.isEnabled shouldBe false
      webHookSettings.uris shouldBe List("fooUrl")

    }


  }

}
