package com.itylos.core.domain

import com.itylos.core.exception.ParameterMissingException
import org.joda.time.DateTimeUtils
import org.json4s.JsonAST._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.domain.Sensor]]
 */
class SensorTest extends WordSpecLike with Matchers with BeforeAndAfterEach with ScalatestRouteTest {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Expected json fields
  val oid = JField("oid", new JString("oid"))
  val sensorId = JField("sensorId", new JString("sensorId"))
  val name = JField("name", new JString("name"))
  val description = JField("description", new JString("description"))
  val location = JField("location", new JString("location"))
  val sensorTypeId = JField("sensorTypeId", new JString("sensorTypeId"))
  val isActive = JField("isActive", new JBool(false))

  "A Sensor" must {
    "throw exception when id is missing and it's required" in {
      val jObject = JObject(List(sensorId, name, description, location, sensorTypeId, isActive))
      val sensor = new Sensor()
      intercept[ParameterMissingException] {
        sensor.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when sensorId is missing" in {
      val jObject = JObject(List(oid, name, description, location, sensorTypeId, isActive))
      val sensor = new Sensor()
      intercept[ParameterMissingException] {
        sensor.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when name is missing" in {
      val jObject = JObject(List(oid, sensorId, description, location, sensorTypeId, isActive))
      val sensor = new Sensor()
      intercept[ParameterMissingException] {
        sensor.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when description is missing" in {
      val jObject = JObject(List(oid, sensorId, name, location, sensorTypeId, isActive))
      val sensor = new Sensor()
      intercept[ParameterMissingException] {
        sensor.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when location is missing" in {
      val jObject = JObject(List(oid, sensorId, name, description, sensorTypeId, isActive))
      val sensor = new Sensor()
      intercept[ParameterMissingException] {
        sensor.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when sensorTypeId is missing" in {
      val jObject = JObject(List(oid, sensorId, name, description, location, isActive))
      val sensor = new Sensor()
      intercept[ParameterMissingException] {
        sensor.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when isActive is missing" in {
      val jObject = JObject(List(oid, sensorId, name, description, location, sensorTypeId))
      val sensor = new Sensor()
      intercept[ParameterMissingException] {
        sensor.fromJObject(jObject, isIdRequired = true)
      }
    }
    "setup sensor event from json data" in {
      val jObject = JObject(List(oid, sensorId, name, description, location, sensorTypeId, isActive))
      val sensor = new Sensor()
      sensor.fromJObject(jObject, isIdRequired = true)
      sensor.oid shouldBe Some("oid")
      sensor.sensorId shouldBe "sensorId"
      sensor.name shouldBe "name"
      sensor.description shouldBe "description"
      sensor.location shouldBe "location"
      sensor.sensorTypeId shouldBe "sensorTypeId"
      sensor.isActive shouldBe false
    }


  }

}
