package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit._
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.service.protocol._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.StatisticsServiceActor]]
 */
class StatisticsServiceActorTest extends TestKit(ActorSystem("testActorSystem")) with ImplicitSender
with DefaultTimeout with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll
with BeforeAndAfterEach {

  // Create the actor to test
  val actorRef = TestActorRef(Props(new StatisticsServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val sensorId = "200"
  val sensorOId = "sensorOId"
  val sensorEvent = new SensorEvent(None, sensorId, 1, 100, None, 1439383870117L)
  val sensor = Sensor(Some(sensorOId), sensorId, "sName", "sDesc", "sLoc", "1", isActive = true, 1000L)
  val sensorType = SensorType("1", "name", "description", isBatteryPowered = true)


  // Reset DAOs
  override def beforeEach(): Unit = {
    reset(sensorEventsStatisticsDao)
  }

  // TODO some tests are failing only on travis :(

  "A StatisticsServiceActor" must {
    "create statistics" in {
      val expectedMinutelyStats = SensorEventsStatistics(None, sensor.sensorId, 1, 1439383800000L) // 5 minute resolution
      val expectedHourlyStats = SensorEventsStatistics(None, sensor.sensorId, 1, 1439380800000L) // Hourly resolution
      val expectedDailyStats = SensorEventsStatistics(None, sensor.sensorId, 1, 1439326800000L) // Daily resolution
      when(sensorEventsStatisticsDao.getMinutelyStatsForSensor(sensor.sensorId, 1439383800000L)).thenReturn(None)
      when(sensorEventsStatisticsDao.getHourlyStatsForSensor(sensor.sensorId, 1439380800000L)).thenReturn(None)
      when(sensorEventsStatisticsDao.getDailyStatsForSensor(sensor.sensorId, 1439326800000L)).thenReturn(None)
      actorRef ! NewSensorEventNotification(sensor, sensorEvent)
      verify(sensorEventsStatisticsDao).saveToMinutely(expectedMinutelyStats)
      verify(sensorEventsStatisticsDao).saveToHourly(expectedHourlyStats)
//      verify(sensorEventsStatisticsDao).saveToDaily(expectedDailyStats)
    }
    "should update minutely statistics" in {
      val expectedMinutelyStats = SensorEventsStatistics(None, sensor.sensorId, 1, 1439383800000L) // 5 minute resolution
      val expectedHourlyStats = SensorEventsStatistics(None, sensor.sensorId, 1, 1439380800000L) // Hourly resolution
      val expectedDailyStats = SensorEventsStatistics(None, sensor.sensorId, 1, 1439326800000L) // Daily resolution
      when(sensorEventsStatisticsDao.getMinutelyStatsForSensor(sensor.sensorId, 1439383800000L)).thenReturn(Some(expectedMinutelyStats))
      when(sensorEventsStatisticsDao.getHourlyStatsForSensor(sensor.sensorId, 1439380800000L)).thenReturn(Some(expectedHourlyStats))
      when(sensorEventsStatisticsDao.getDailyStatsForSensor(sensor.sensorId, 1439326800000L)).thenReturn(Some(expectedDailyStats))
      actorRef ! NewSensorEventNotification(sensor, sensorEvent)
      verify(sensorEventsStatisticsDao).updateMinutely(expectedMinutelyStats)
      verify(sensorEventsStatisticsDao).updateHourly(expectedHourlyStats)
//      verify(sensorEventsStatisticsDao).updateDaily(expectedDailyStats)
      expectedMinutelyStats.sensorEventsCount shouldBe 2
      expectedHourlyStats.sensorEventsCount shouldBe 2
//      expectedDailyStats.sensorEventsCount shouldBe 2
    }
  }
}
