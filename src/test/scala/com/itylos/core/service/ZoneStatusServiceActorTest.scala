package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.exception.ZoneDoesNotExistException
import com.itylos.core.rest.dto.ZoneStatusDto
import com.itylos.core.service.protocol._
import org.joda.time.DateTimeUtils
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.ZoneStatusServiceActor]]
 */
class ZoneStatusServiceActorTest extends TestKit(ActorSystem("testActorSystem")) with ImplicitSender
with DefaultTimeout with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll
with BeforeAndAfterEach {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Create the actor to test
  val actorRef = TestActorRef(Props(new ZoneStatusServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val zoneId = "fooZoneId"
  val user = new User(Some("userOid"), "userName", "userEmail", List(), "webPass", "alarmPass")
  val updatedZoneStatus = ZoneStatus(zoneId, ENABLED)
  val zoneData = Zone(Some(zoneId), "zName", "zDesc", List("sensorOid"), 15L)

  // Expected responses
  val zoneStatusDtoResponse = List(new ZoneStatusDto(updatedZoneStatus, zoneData))

  // Reset DAOs
  override def beforeEach(): Unit = {
    reset(zoneStatusDao)
    reset(zoneDao)
  }


  "A ZoneStatusServiceActor" must {
    "throw exception when zone id is not valid" in {
      when(zoneDao.checkZonesExistence(List(zoneId))).thenThrow(new ZoneDoesNotExistException(zoneId))
      intercept[ZoneDoesNotExistException] {
        actorRef.receive(UpdateZoneStatus(user, updatedZoneStatus))
      }
      expectNoMsg()
    }
    "update zone status" in {
      when(zoneStatusDao.getAllZonesStatus).thenReturn(List(updatedZoneStatus))
      when(zoneDao.getZoneByObjectId(updatedZoneStatus.zoneId)).thenReturn(Some(zoneData))
      actorRef ! UpdateZoneStatus(user, updatedZoneStatus)
      verify(zoneDao).checkZonesExistence(List(zoneId))
      verify(zoneStatusDao).update(updatedZoneStatus)
      expectMsg(GetZoneStatusRs(zoneStatusDtoResponse))
    }
    "get status for each zone" in {
      val thisUpdatedZoneStatus = ZoneStatus(zoneId, DISABLED)
      val thisZoneStatusDtoResponse = List(new ZoneStatusDto(thisUpdatedZoneStatus, zoneData))
      when(zoneStatusDao.getAllZonesStatus).thenReturn(List(ZoneStatus(zoneId, DISABLED)))
      when(zoneDao.getZoneByObjectId(updatedZoneStatus.zoneId)).thenReturn(Some(zoneData))
      actorRef ! GetCurrentZoneStatusRq(user)
      expectMsg(GetZoneStatusRs(thisZoneStatusDtoResponse))
    }
  }

}
