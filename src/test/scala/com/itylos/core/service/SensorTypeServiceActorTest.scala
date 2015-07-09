package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain.SensorType
import com.itylos.core.service.protocol.{GetAllSensorTypesRq, GetAllSensorTypesRs, LoadSensorTypes}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.SensorTypeServiceActor]]
 */
class SensorTypeServiceActorTest extends TestKit(ActorSystem("testsystem")) with ImplicitSender with DefaultTimeout
with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll with BeforeAndAfterEach {

  // Create the actor to test
  val actorRef = TestActorRef(Props(new SensorTypeServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val sensorTypes = List(SensorType("id", "name", "description", isBatteryPowered = true))

  override def beforeEach(): Unit = {
    reset(sensorTypeDao)
  }

  "A SensorTypeService Actor" must {
    "list all available sensor types" in {
      when(sensorTypeDao.getAllSensorTypes).thenReturn(sensorTypes)
      actorRef ! GetAllSensorTypesRq()
      expectMsg(GetAllSensorTypesRs(sensorTypes))
    }
    "load sensor types from file if they do not exist" in {
      when(sensorTypeDao.getSensorTypeByObjectId("1")).thenReturn(None)
      actorRef ! LoadSensorTypes()
      verify(sensorTypeDao, times(1)).save(any())
      expectNoMsg()
    }
    "not load sensor types from file if they already exists" in {
      when(sensorTypeDao.getSensorTypeByObjectId("1")).thenReturn(Some(sensorTypes.head))
      actorRef ! LoadSensorTypes()
      verify(sensorTypeDao, never()).save(any())
      expectNoMsg()
    }
  }

}
