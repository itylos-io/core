package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.exception.{SensorDoesNotExistException, ZoneDoesNotExistException}
import com.itylos.core.rest.dto.{SensorDto, ZoneDto}
import com.itylos.core.service.protocol._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.ZoneServiceActor]]
 */
class ZoneServiceActorTest extends TestKit(ActorSystem("testActorSystem")) with ImplicitSender
with DefaultTimeout with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll
with BeforeAndAfterEach {

  // Create the actor to test
  val actorRef = TestActorRef(Props(new ZoneServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val zoneId = "zoneId"
  val sensorOid = "sensorOId"
  val sensorOIds = List(sensorOid)
  val user = new User(Some("userOid"), "userName", "userEmail", List(), "webPass", "alarmPass")
  var zone = Zone(Some(zoneId), "zName", "zDesc", sensorOIds, 15L)

  // Setup expected responses
  val zoneStatus = ZoneStatus(zoneId, ENABLED, 15L)
  val sensor = Sensor(Some(sensorOid), "sensorId", "sName", "sDesc", "sLoc", "1", isActive = true, 15L)
  val sensorType = SensorType("1", "stName", "stDesc", isBatteryPowered = true)
  val sensorsDto = new SensorDto(sensor, sensorType, None)
  val zonesDtoResponse = List(new ZoneDto(zone, zoneStatus, Some(List(sensorsDto))))

  // Reset DAOs
  override def beforeEach(): Unit = {
    reset(zoneStatusDao)
    reset(zoneDao)
    reset(sensorTypeDao)
    reset(sensorDao)
  }


  "A ZoneServiceActor" must {
    "save a new zone" in {
      when(zoneDao.getAllZones).thenReturn(List(zone))
      when(sensorDao.getSensorByObjectId(zone.sensorOIds.head)).thenReturn(Some(sensor))
      when(sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId)).thenReturn(Some(sensorType))
      when(zoneStatusDao.getZoneStatusByZoneId(zone.oid.get)).thenReturn(Some(zoneStatus))
      actorRef ! CreateZoneRq(user, zone)
      verify(sensorDao).checkSensorsExistenceByOid(zone.sensorOIds)
      verify(zoneDao).save(zone)
      verify(zoneStatusDao).save(zoneStatus)
      expectMsg(GetZonesRs(zonesDtoResponse))
    }
    "update a zone" in {
      when(zoneDao.getAllZones).thenReturn(List(zone))
      when(sensorDao.getSensorByObjectId(zone.sensorOIds.head)).thenReturn(Some(sensor))
      when(sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId)).thenReturn(Some(sensorType))
      when(zoneStatusDao.getZoneStatusByZoneId(zone.oid.get)).thenReturn(Some(zoneStatus))
      actorRef ! UpdateZoneRq(user, zone)
      verify(zoneDao).update(zone)
      expectMsg(GetZonesRs(zonesDtoResponse))
    }
    "delete a zone" in {
      when(zoneDao.getAllZones).thenReturn(List())
      actorRef ! DeleteZoneRq(user, zone.oid.get)
      verify(zoneStatusDao).deleteZoneStatusByZoneId(zoneId)
      verify(zoneDao).deleteZoneByObjectId(zone.oid.get)
      expectMsg(GetZonesRs(List()))
    }
    "remove sensor from zones" in {
      val z = zone
      when(zoneDao.getAllZones).thenReturn(List(z))
      actorRef ! RemoveSensorFromZone(sensorOid)
      val updateZone = Zone(Some(zoneId), "zName", "zDesc", List(), 15L)
      verify(zoneDao).update(updateZone)
    }
    "get all zones" in {
      val z = Zone(Some(zoneId), "zName", "zDesc", sensorOIds, 15L)
      when(zoneDao.getAllZones).thenReturn(List(z))
      when(sensorDao.getSensorByObjectId(z.sensorOIds.head)).thenReturn(Some(sensor))
      when(sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId)).thenReturn(Some(sensorType))
      when(zoneStatusDao.getZoneStatusByZoneId(z.oid.get)).thenReturn(Some(zoneStatus))
      actorRef ! GetZonesRq(user)
      expectMsg(GetZonesRs(zonesDtoResponse))
    }
    "throw exception when creating zone and sensor does not exist" in {
      when(sensorDao.checkSensorsExistenceByOid(zone.sensorOIds)).thenThrow(new SensorDoesNotExistException(sensorOIds.head))
      intercept[SensorDoesNotExistException] {
        actorRef.receive(CreateZoneRq(user, zone))
      }
      expectNoMsg()
    }
    "throw exception when updating zone and sensor does not exist" in {
      when(sensorDao.checkSensorsExistenceByOid(zone.sensorOIds)).thenThrow(new SensorDoesNotExistException(sensorOIds.head))
      intercept[SensorDoesNotExistException] {
        actorRef.receive(UpdateZoneRq(user, zone))
      }
      expectNoMsg()
    }
    "throw exception when updating zone and zone does not exist" in {
      when(zoneDao.checkZonesExistence(List(zoneId))).thenThrow(new ZoneDoesNotExistException(zoneId))
      intercept[ZoneDoesNotExistException] {
        actorRef.receive(UpdateZoneRq(user, zone))
      }
      expectNoMsg()
    }
  }

}
