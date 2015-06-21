package com.itulos.controller.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itulos.controller.StopSystemAfterAll
import com.itulos.controller.dao.TestEnvironmentRepos
import com.itulos.controller.domain.SensorType
import com.itulos.controller.service.protocol.{LoadSensorTypes, GetAllSensorTypesRq, GetAllSensorTypesRs}
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import org.mockito.Matchers.any

class SensorTypeServiceActorTest extends TestKit(ActorSystem("testsystem")) with ImplicitSender with DefaultTimeout
with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    reset(sensorTypeDao)
  }

  val actorRef = TestActorRef(Props(new SensorTypeServiceActor() with TestEnvironmentRepos {}))

  val sensorTypes = List(SensorType("id", "name", "description"))


  "A SensorTypeService Actor" must {
    "list all available sensor types" in {
      when(sensorTypeDao.getAllSensorTypes).thenReturn(sensorTypes)
      actorRef ! GetAllSensorTypesRq()
      expectMsg(GetAllSensorTypesRs(sensorTypes))
    }
    "load sensor types from file if they do not exist" in {
      when(sensorTypeDao.getSensorTypeByObjectId("1")).thenReturn(None)
      actorRef ! LoadSensorTypes()
      verify(sensorTypeDao,times(1)).save(any())
      expectNoMsg()
    }
    "not load sensor types from file if they already exists" in {
      when(sensorTypeDao.getSensorTypeByObjectId("1")).thenReturn(Some(sensorTypes.head))
      actorRef ! LoadSensorTypes()
      verify(sensorTypeDao,never()).save(any())
      expectNoMsg()
    }
  }

}
