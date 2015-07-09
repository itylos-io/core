package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit._
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.exception.{SensorDoesNotExistException, SensorTypeDoesNotExistException}
import com.itylos.core.rest.dto.SensorEventDto
import com.itylos.core.service.protocol._
import org.joda.time.DateTimeUtils
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.SensorEventServiceActor]]
 */
class SensorEventServiceActorTest extends TestKit(ActorSystem("testActorSystem")) with ImplicitSender
with DefaultTimeout with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll
with BeforeAndAfterEach {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Create the actor to test
  val actorRef = TestActorRef(Props(new SensorEventServiceActor() with TestEnvironmentRepos with NotificationsHelper {}))

  // Common variables to all tests
  val sensorId = "200"
  val sensorOId = "sensorOId"
  val sensorEvent = new SensorEvent(None, sensorId, OPEN, 100, 1000L)
  val sensor = Sensor(Some(sensorOId), sensorId, "sName", "sDesc", "sLoc", "1", isActive = true, 1000L)
  val sensorType = SensorType("1", "name", "description", isBatteryPowered = true)

  // Expected responses
  val sensorEventsDto = List(new SensorEventDto(sensorEvent, sensor))

  // Since SensorEventServiceActor uses notifier we need to make sure messages get delivered to corresponding actors
  val soundServiceActorProbe = TestProbe()
  val temp1 = TestActorRef(Props(new ForwardActor(soundServiceActorProbe.ref)), "soundServiceActor")
  val webSocketActorProbe = TestProbe()
  val temp2 = TestActorRef(Props(new ForwardActor(webSocketActorProbe.ref)), "webSocketActor")
  val pushBulletServiceActorProbe = TestProbe()
  val temp3 = TestActorRef(Props(new ForwardActor(pushBulletServiceActorProbe.ref)), "pushBulletServiceActor")
  val webHooksServiceActorProbe = TestProbe()
  val temp4 = TestActorRef(Props(new ForwardActor(webHooksServiceActorProbe.ref)), "webHooksServiceActor")

  // Reset DAOs
  override def beforeEach(): Unit = {
    reset(sensorEventDao)
    reset(sensorDao)
  }

  "A SensorEventServiceActor" must {
    "throw exception when submitting sensor event and sensor does not exist" in {
      when(sensorDao.getSensorBySensorId(sensorEvent.sensorId)).thenReturn(None)
      intercept[SensorDoesNotExistException] {
        actorRef.receive(AddSensorEventRq(sensorEvent))
      }
      expectNoMsg()
    }
  }
  "throw exception when submitting sensor event and sensor type does not exist" in {
    when(sensorDao.getSensorBySensorId(sensorEvent.sensorId)).thenReturn(Some(sensor))
    when(sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId)).thenReturn(None)
    intercept[SensorTypeDoesNotExistException] {
      actorRef.receive(AddSensorEventRq(sensorEvent))
    }
    expectNoMsg()
  }
  "submit new sensor and set battery to -1 if sensor is not battery powered" in {
    val thisSensorType = SensorType("1", "name", "description", isBatteryPowered = false)
    val thisSensorEvent = new SensorEvent(None, sensorId, OPEN, 100, 1000L)
    when(sensorDao.getSensorBySensorId(sensorEvent.sensorId)).thenReturn(Some(sensor))
    when(sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId)).thenReturn(Some(thisSensorType))
    actorRef ! AddSensorEventRq(thisSensorEvent)
    val expectedMessage = new SensorEvent(None, sensorId, OPEN, -1, 1000L)
    soundServiceActorProbe.expectMsg(new NewSensorEventNotification(sensor, expectedMessage))
    webSocketActorProbe.expectMsg(Event(new NewSensorEventNotification(sensor, expectedMessage)))
    pushBulletServiceActorProbe.expectMsg(new NewSensorEventNotification(sensor, expectedMessage))
    webHooksServiceActorProbe.expectMsg(new NewSensorEventNotification(sensor, expectedMessage))
    verify(sensorEventDao).save(expectedMessage)
    expectMsg(GetSensorEventsRs(List()))
  }
  "submit new sensor" in {
    when(sensorDao.getSensorBySensorId(sensorEvent.sensorId)).thenReturn(Some(sensor))
    when(sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId)).thenReturn(Some(sensorType))
    actorRef ! AddSensorEventRq(sensorEvent)
    soundServiceActorProbe.expectMsg(new NewSensorEventNotification(sensor, sensorEvent))
    webSocketActorProbe.expectMsg(Event(new NewSensorEventNotification(sensor, sensorEvent)))
    pushBulletServiceActorProbe.expectMsg(new NewSensorEventNotification(sensor, sensorEvent))
    webHooksServiceActorProbe.expectMsg(new NewSensorEventNotification(sensor, sensorEvent))
    verify(sensorEventDao).save(sensorEvent)
    expectMsg(GetSensorEventsRs(List()))
  }
  "get sensor events" in {
    when(sensorDao.getSensorBySensorId(sensorEvent.sensorId)).thenReturn(Some(sensor))
    when(sensorEventDao.getSensorEvents(Some(sensorId), 100, 0)).thenReturn(List(sensorEvent))
    actorRef ! GetSensorEventsRq(Some(sensorId), 100, 0)
    expectMsg(GetSensorEventsRs(sensorEventsDto))
  }
  "get latest event for each sensor" in {
    val secondSensorId = "2ndSensorId"
    when(sensorDao.getSensorBySensorId(sensorEvent.sensorId)).thenReturn(Some(sensor))
    when(sensorEventDao.getLatestSensorEvent(secondSensorId)).thenReturn(None)
    when(sensorEventDao.getLatestSensorEvent(sensorId)).thenReturn(Some(sensorEvent))
    actorRef ! GetSensorLatestEventsRq(List(sensorId, "2ndSensorId"))
    expectMsg(GetSensorEventsRs(sensorEventsDto))
  }
  "should throw exception when trying to remove sensor events of an non existing sensor" in {
    when(sensorDao.checkSensorsExistenceByOid(List(sensorOId))).thenThrow(new SensorDoesNotExistException(sensorId))
    intercept[SensorDoesNotExistException] {
      actorRef.receive(RemoveSensorEventsForSensor(sensorOId))
    }
    expectNoMsg()
  }
  "should remove sensor events associated to a sensor" in {
    when(sensorDao.getSensorBySensorId(sensorEvent.sensorId)).thenReturn(Some(sensor))
    when(sensorDao.getSensorByObjectId(sensorOId)).thenReturn(Some(sensor))
    when(sensorEventDao.getSensorEvents(None, 5, 0)).thenReturn(List(sensorEvent))
    actorRef ! RemoveSensorEventsForSensor(sensorOId)
    //    soundServiceProbe.expectNoMsg()
    verify(sensorEventDao).removeEventsForSensor(sensor.sensorId)
    expectMsg(GetSensorEventsRs(sensorEventsDto))
  }

}
