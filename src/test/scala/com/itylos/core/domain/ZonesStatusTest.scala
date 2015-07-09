package com.itylos.core.domain

import com.itylos.core.exception.ParameterMissingException
import org.joda.time.DateTimeUtils
import org.json4s.JsonAST.{JField, JObject, JString}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.domain.ZoneStatus]]
 */
class ZonesStatusTest extends WordSpecLike with Matchers with BeforeAndAfterEach with ScalatestRouteTest {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  "A ZoneStatus" must {
    "throw exception when zone ID is missing" in {
      val jObject = JObject(List())
      val zoneStatus = new ZoneStatus()
      intercept[ParameterMissingException] {
        zoneStatus.fromJObject(jObject)
      }
    }
    "throw exception when status is missing" in {
      val zoneId = JField("zoneId", new JString("zoneId"))
      val jObject = JObject(List(zoneId))
      val zoneStatus = new ZoneStatus()
      intercept[ParameterMissingException] {
        zoneStatus.fromJObject(jObject)
      }
    }
    "be populated with data from a JObject" in {
      val zoneId = JField("zoneId", new JString("zoneId"))
      val status = JField("status", new JString("ENABLED"))
      val jObject = JObject(List(zoneId, status))

      val zoneStatus = new ZoneStatus()
      zoneStatus.fromJObject(jObject)
      zoneStatus.zoneId shouldBe "zoneId"
      zoneStatus.status shouldBe ENABLED
      zoneStatus.dateUpdated shouldBe 1000L
    }
  }

}
