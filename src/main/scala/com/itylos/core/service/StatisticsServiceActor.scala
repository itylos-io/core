package com.itylos.core.service


import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao.{SensorComponent, SensorEventsStatisticsComponent}
import com.itylos.core.domain.{Sensor, SensorEventsStatistics}
import com.itylos.core.rest.dto.SensorEventsStatisticsDto
import com.itylos.core.service.protocol._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A companion object to properly initiate [[StatisticsServiceActor]]
 */
object StatisticsServiceActor {
  def props(): Props = {
    Props(new StatisticsServiceActor() with SensorEventsStatisticsComponent with SensorComponent {
      val sensorEventsStatisticsDao = new SensorEventsStatisticsDao
      val sensorDao = new SensorDao
    })
  }
}

/**
 * An actor responsible for sending notifications through PushBullet Service
 */
class StatisticsServiceActor extends Actor with ActorLogging {
  this: SensorEventsStatisticsComponent with SensorComponent =>
  override def preStart() {
    import scala.concurrent.duration._
    log.info("Starting statistics service...")
    context.system.scheduler.schedule(1.seconds, 60.seconds, self, RemoveExpiredStats())
  }

  def receive = {

    // --- New sensor event --- //
    case NewSensorEventNotification(sensor, sensorEvent, kerberosImages) =>

      // Setup for minutely collection
      val minutes = new DateTime(sensorEvent.dateOfEvent).minuteOfHour().get()
      val nearestMultipleOf5 = (5 * Math.floor(Math.abs(minutes / 5))).toInt
      val nearestMultipleOf5DocId = new DateTime(sensorEvent.dateOfEvent).withMinuteOfHour(nearestMultipleOf5).withSecondOfMinute(0).withMillisOfSecond(0)
      // Setup for hourly document
      val nearestHourDocId = new DateTime(sensorEvent.dateOfEvent).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
      // Setup for daily document
      val nearestDayDocId = new DateTime(sensorEvent.dateOfEvent).withMillisOfDay(0)

      addToMinutelyStats(sensor, nearestMultipleOf5DocId.getMillis)
      addToHourlyStats(sensor, nearestHourDocId.getMillis)
      addToDailyStats(sensor, nearestDayDocId.getMillis)

    // --- Get stats for all sensors --- //
    case GetStats() =>
      sender() ! GetStatisticsRs(
        convert2DTOs(sensorEventsStatisticsDao.getMinutelyStats),
        convert2DTOs(sensorEventsStatisticsDao.getHourlyStats),
        convert2DTOs(sensorEventsStatisticsDao.getDailyStats))

    // --- Remove expired statistics --- //
    case RemoveExpiredStats() =>
      sensorEventsStatisticsDao.deleteMinutelyStatsBefore(new DateTime().minusMinutes(60).getMillis)
      sensorEventsStatisticsDao.deleteHourlyStatsBefore(new DateTime().minusHours(24).getMillis)
      sensorEventsStatisticsDao.deleteDailyStatsBefore(new DateTime().minusDays(7).getMillis)

  }

  /**
   * Convert [[com.itylos.core.domain.SensorEventsStatistics]] to [[com.itylos.core.rest.dto.SensorEventsStatisticsDto]]
   */
  private def convert2DTOs(stats: List[SensorEventsStatistics]): List[SensorEventsStatisticsDto] = {
    for (stat <- stats) yield {
      new SensorEventsStatisticsDto(sensorDao.getSensorBySensorId(stat.sensorId).get, stat)
    }
  }

  /**
   * Update minutely statistics collection for a specific sensor
   */
  def addToMinutelyStats(sensor: Sensor, datetime: Long): Unit = {
    val stats = sensorEventsStatisticsDao.getMinutelyStatsForSensor(sensor.sensorId, datetime)

    if (stats == None) {
      val newStats = SensorEventsStatistics(None, sensor.sensorId, 1, datetime)
      sensorEventsStatisticsDao.saveToMinutely(newStats)
    } else {
      stats.get.incrementSensorCount()
      sensorEventsStatisticsDao.updateMinutely(stats.get)
    }
  }

  /**
   * Update hourly statistics collection for a specific sensor
   */
  def addToHourlyStats(sensor: Sensor, datetime: Long): Unit = {
    val stats = sensorEventsStatisticsDao.getHourlyStatsForSensor(sensor.sensorId, datetime)
    if (stats == None) {
      val newStats = SensorEventsStatistics(None, sensor.sensorId, 1, datetime)
      sensorEventsStatisticsDao.saveToHourly(newStats)
    } else {
      stats.get.incrementSensorCount()
      sensorEventsStatisticsDao.updateHourly(stats.get)
    }
  }

  /**
   * Update daily statistics collection for a specific sensor
   */
  def addToDailyStats(sensor: Sensor, datetime: Long): Unit = {
    val stats = sensorEventsStatisticsDao.getDailyStatsForSensor(sensor.sensorId, datetime)
    if (stats == None) {
      val newStats = SensorEventsStatistics(None, sensor.sensorId, 1, datetime)
      sensorEventsStatisticsDao.saveToDaily(newStats)
    } else {
      stats.get.incrementSensorCount()
      sensorEventsStatisticsDao.updateDaily(stats.get)
    }
  }

}