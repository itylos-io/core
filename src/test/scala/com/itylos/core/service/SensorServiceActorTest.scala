package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.exception.{SensorIdAlreadyExistsException, SensorTypeDoesNotExistException}
import com.itylos.core.rest.dto.{SensorDto, ZoneDto}
import com.itylos.core.service.protocol._
import org.joda.time.DateTimeUtils
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.SensorServiceActor]]
 */
class SensorServiceActorTest extends TestKit(ActorSystem("testActorSystem")) with ImplicitSender
with DefaultTimeout with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll
with BeforeAndAfterEach {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Create the actor to test
  val actorRef = TestActorRef(Props(new SensorServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val sensorOid = "sensorOId"
  val sensorId = "sensorId"
  val user = new User(Some("userOid"), "userName", "userEmail",  "webPass", "alarmPass")
  val sensor = Sensor(Some(sensorOid), sensorId, "sName", "sDesc", "sLoc", "1", isActive = true, 1000L)
  val sensorType = SensorType("1", "name", "description", isBatteryPowered = true)

  // Expected responses
  val zoneId = "fooZoneId"
  val zone = Zone(Some(zoneId), "zName", "zDesc", List(sensorOid), 15L)
  val zoneStatus = ZoneStatus(zoneId, ENABLED)
  val sensorDto = new SensorDto(sensor, sensorType, Some(List(new ZoneDto(zone, zoneStatus, None))))


  // Reset DAOs
  override def beforeEach(): Unit = {
    reset(zoneDao)
    reset(sensorTypeDao)
    reset(sensorDao)
  }


  "A SensorServiceActor" must {
    "throw exception when creating sensor and sensor id is already assigned to another sensor" in {
      when(sensorDao.getSensorBySensorId(sensorId)).thenReturn(Some(sensor))
      intercept[SensorIdAlreadyExistsException] {
        actorRef.receive(CreateSensorRq(sensor))
      }
      expectNoMsg()
    }
    "throw exception when creating sensor and sensor type id is not valid " in {
      when(sensorTypeDao.checkSensorTypesExistenceBySensorId(List(sensor.sensorTypeId))).thenThrow(new SensorTypeDoesNotExistException(sensor.sensorTypeId))
      when(sensorDao.getSensorBySensorId(sensorId)).thenReturn(None)
      intercept[SensorTypeDoesNotExistException] {
        actorRef.receive(CreateSensorRq(sensor))
      }
      expectNoMsg()
    }
    "create new sensor" in {
      when(sensorDao.getAllSensor).thenReturn(List(sensor))
      when(zoneDao.getZonesForSensorOid(sensor.oid.get)).thenReturn(List(zone))
      when(zoneStatusDao.getZoneStatusByZoneId(zone.oid.get)).thenReturn(Some(zoneStatus))
      when(sensorDao.getSensorBySensorId(sensorId)).thenReturn(None)
      when(sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId)).thenReturn(Some(sensorType))
      actorRef ! CreateSensorRq(sensor)
      verify(sensorDao).save(sensor)
      expectMsg(GetAllSensorRs(List(sensorDto)))
    }
    "throw exception when updating sensor and updated sensor id is already assigned to another sensor" in {
      val existingSensor = Sensor(Some("500"), sensorId, "sName", "sDesc", "sLoc", "1", isActive = true, 1000L)
      when(sensorDao.getSensorBySensorId(sensorId)).thenReturn(Some(existingSensor))
      intercept[SensorIdAlreadyExistsException] {
        actorRef.receive(UpdateSensorRq(sensor))
      }
      verify(sensorDao, times(0)).update(sensor)
      expectNoMsg()
    }
    "update sensor" in {
      when(sensorDao.getAllSensor).thenReturn(List(sensor))
      when(zoneDao.getZonesForSensorOid(sensor.oid.get)).thenReturn(List(zone))
      when(zoneStatusDao.getZoneStatusByZoneId(zone.oid.get)).thenReturn(Some(zoneStatus))
      when(sensorDao.getSensorBySensorId(sensorId)).thenReturn(None)
      when(sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId)).thenReturn(Some(sensorType))
      actorRef ! UpdateSensorRq(sensor)
      verify(sensorDao).update(sensor)
      expectMsg(GetAllSensorRs(List(sensorDto)))
    }
    "delete sensor" in {
      when(sensorDao.getAllSensor).thenReturn(List(sensor))
      when(zoneDao.getZonesForSensorOid(sensor.oid.get)).thenReturn(List(zone))
      when(zoneStatusDao.getZoneStatusByZoneId(zone.oid.get)).thenReturn(Some(zoneStatus))
      when(sensorDao.getSensorBySensorId(sensorId)).thenReturn(None)
      when(sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId)).thenReturn(Some(sensorType))
      actorRef ! DeleteSensorRq(sensorOid)
      verify(sensorDao).deleteSensorByObjectId(sensorOid)
      expectMsg(GetAllSensorRs(List(sensorDto)))
    }
    "get all sensors" in {
      when(sensorDao.getAllSensor).thenReturn(List(sensor))
      when(zoneDao.getZonesForSensorOid(sensor.oid.get)).thenReturn(List(zone))
      when(zoneStatusDao.getZoneStatusByZoneId(zone.oid.get)).thenReturn(Some(zoneStatus))
      when(sensorDao.getSensorBySensorId(sensorId)).thenReturn(None)
      when(sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId)).thenReturn(Some(sensorType))
      actorRef ! GetAllSensorsRq()
      expectMsg(GetAllSensorRs(List(sensorDto)))
    }
  }

}
