package com.itylos.core.domain

import com.itylos.core.exception.ParameterMissingException
import org.joda.time.DateTimeUtils
import org.json4s.JsonAST._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.domain.SensorEvent]]
 */
class SensorEventTest extends WordSpecLike with Matchers with BeforeAndAfterEach with ScalatestRouteTest {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Expected json fields
  val oid = JField("oid", new JString("oid"))
  val sensorId = JField("sensorId", new JString("sensorId"))
  val batteryLevel = JField("batteryLevel", new JInt(85))
  val sensorStatus = JField("status", new JInt(1))

  "A SensorEvent" must {
    "throw exception when id is missing and it's required" in {
      val jObject = JObject(List(sensorId, sensorStatus, batteryLevel))
      val event = new SensorEvent()
      intercept[ParameterMissingException] {
        event.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when sensorId is missing " in {
      val jObject = JObject(List(oid, sensorStatus, batteryLevel))
      val event = new SensorEvent()
      intercept[ParameterMissingException] {
        event.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when sensorStatus is missing" in {
      val jObject = JObject(List(oid, sensorId, batteryLevel))
      val event = new SensorEvent()
      intercept[ParameterMissingException] {
        event.fromJObject(jObject, isIdRequired = true)
      }
    }
    "setup sensor event from json data when batteryLevel missing" in {
      val jObject = JObject(List(oid, sensorId, sensorStatus))
      val event = new SensorEvent()
      event.fromJObject(jObject, isIdRequired = true)
      event.oid shouldBe Some("oid")
      event.sensorId shouldBe "sensorId"
      event.status shouldBe 1
      event.batteryLevel shouldBe -1
      event.dateOfEvent shouldBe 1000L
    }
    "setup sensor event from json data" in {
      val jObject = JObject(List(oid, sensorId, sensorStatus, batteryLevel))
      val event = new SensorEvent()
      event.fromJObject(jObject, isIdRequired = true)
      event.oid shouldBe Some("oid")
      event.sensorId shouldBe "sensorId"
      event.status shouldBe 1
      event.batteryLevel shouldBe 85
      event.dateOfEvent shouldBe 1000L
    }


  }

}
