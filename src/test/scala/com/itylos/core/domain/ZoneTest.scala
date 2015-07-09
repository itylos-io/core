package com.itylos.core.domain

import com.itylos.core.exception.ParameterMissingException
import org.joda.time.DateTimeUtils
import org.json4s.JsonAST.{JArray, JField, JObject, JString}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

/**
 * Tests [[com.itylos.core.domain.Zone]]
 */
class ZoneTest extends WordSpecLike with Matchers with BeforeAndAfterEach with ScalatestRouteTest {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Expected json fields
  val zoneId = JField("oid", new JString("oid"))
  val zoneName = JField("name", new JString("name"))
  val zoneDesc = JField("description", new JString("description"))
  val zoneSensorOIds = JField("sensorOIds", new JArray(List(new JString("sensorOid"))))


  "A Zone" must {
    "throw exception when zone oid is required and is missing" in {
      val jObject = JObject(List(zoneName, zoneDesc, zoneSensorOIds))
      val zone = new Zone()
      intercept[ParameterMissingException] {
        zone.fromJObject(jObject, isIdRequired = true)
      }
    }
    "throw exception when zone name is missing" in {
      val jObject = JObject(List(zoneId, zoneDesc, zoneSensorOIds))
      val zone = new Zone()
      intercept[ParameterMissingException] {
        zone.fromJObject(jObject, isIdRequired = false)
      }
    }
    "throw exception when zone description is missing" in {
      val jObject = JObject(List(zoneId, zoneName, zoneSensorOIds))
      val zone = new Zone()
      intercept[ParameterMissingException] {
        zone.fromJObject(jObject, isIdRequired = false)
      }
    }
    "throw exception when zone sensor ids are missing" in {
      val jObject = JObject(List(zoneId, zoneName, zoneDesc))
      val zone = new Zone()
      intercept[ParameterMissingException] {
        zone.fromJObject(jObject, isIdRequired = false)
      }
    }
    "setup zone from json data without id" in {
      val jObject = JObject(List(zoneId, zoneName, zoneDesc, zoneSensorOIds))
      val zone = new Zone()
      zone.fromJObject(jObject, isIdRequired = true)
      zone.dateCreated shouldBe 1000L
      zone.oid shouldBe Some("oid")
      zone.name shouldBe "name"
      zone.description shouldBe "description"
      zone.sensorOIds shouldBe List("sensorOid")

    }
    "setup zone from json data with id" in {
      val jObject = JObject(List(zoneName, zoneDesc, zoneSensorOIds))
      val zone = new Zone()
      zone.fromJObject(jObject, isIdRequired = false)
      zone.oid shouldBe None
      zone.name shouldBe "name"
      zone.description shouldBe "description"
      zone.sensorOIds shouldBe List("sensorOid")

    }


  }

}
