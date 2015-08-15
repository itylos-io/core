package com.itylos.core.dao

import java.util.Date

import com.itylos.core.domain.{DaoObject, SensorEventsStatistics}
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._
import org.bson.types.ObjectId

/**
 * Dao for [[com.itylos.core.domain.SensorEventsStatistics]]
 */
trait SensorEventsStatisticsComponent {
  val sensorEventsStatisticsDao: SensorEventsStatisticsDao

  class SensorEventsStatisticsDao extends CommonDao[SensorEventsStatistics] {
    val MINUTELY_COLLECTION_NAME = "minutelySensorStats"
    val HOURLY_COLLECTION_NAME = "hourlySensorStats"
    val DAILY_COLLECTION_NAME = "dailySensorStats"
    val DATE_SORT_FIELD = Some("datetimeInterval")
    val DATE_SORT_ASC = Some(false)

    override def getCollectionName: String = DAILY_COLLECTION_NAME

    /**
     * Fetch all minutely statistics for all sensors
     */
    def getMinutelyStats: List[SensorEventsStatistics] = {
      val stats = db(MINUTELY_COLLECTION_NAME).find()
      if (stats.isEmpty) List() else stats.map(x => new SensorEventsStatistics(x)).toList
    }

    /**
     * Fetch all hourly statistics
     */
    def getHourlyStats: List[SensorEventsStatistics] = {
      val stats = db(HOURLY_COLLECTION_NAME).find()
      if (stats.isEmpty) List() else stats.map(x => new SensorEventsStatistics(x)).toList
    }

    /**
     * Fetch all daily statistics
     */
    def getDailyStats: List[SensorEventsStatistics] = {
      val stats = db(DAILY_COLLECTION_NAME).find()
      if (stats.isEmpty) List() else stats.map(x => new SensorEventsStatistics(x)).toList
    }

    /**
     * Fetch statistics for a specific sensor for a specific minute
     * @param sensorId the id of the sensor to fetch statistics for
     * @param datetime the minute (in millis) for which to fetch statistics
     */
    def getMinutelyStatsForSensor(sensorId: String, datetime: Long): Option[SensorEventsStatistics] = {
      val data = db(MINUTELY_COLLECTION_NAME).findOne(MongoDBObject(
        "sensorId" -> sensorId,
        "datetimeInterval" -> datetime))
      if (data != None) {
        return Some(new SensorEventsStatistics(data.get))
      }
      None
    }

    /**
     * Fetch statistics for a specific sensor for a specific hour
     * @param sensorId the id of the sensor to fetch statistics for
     * @param datetime the hour (in millis) for which to fetch statistics
     */
    def getHourlyStatsForSensor(sensorId: String, datetime: Long): Option[SensorEventsStatistics] = {
      val data = db(HOURLY_COLLECTION_NAME).findOne(MongoDBObject(
        "sensorId" -> sensorId,
        "datetimeInterval" -> datetime))
      if (data != None) {
        return Some(new SensorEventsStatistics(data.get))
      }
      None
    }

    /**
     * Fetch statistics for a specific sensor for a specific day
     * @param sensorId the id of the sensor to fetch statistics for
     * @param datetime the day (in millis) for which to fetch statistics
     */
    def getDailyStatsForSensor(sensorId: String, datetime: Long): Option[SensorEventsStatistics] = {
      val data = db(DAILY_COLLECTION_NAME).findOne(MongoDBObject(
        "sensorId" -> sensorId,
        "datetimeInterval" -> datetime))
      if (data != None) {
        return Some(new SensorEventsStatistics(data.get))
      }
      None
    }

    def deleteMinutelyStatsBefore(datetime: Long): Unit = {
      val q: DBObject = "datetimeInterval" $lt datetime
      db(MINUTELY_COLLECTION_NAME).remove(q)
    }

    def deleteHourlyStatsBefore(datetime: Long): Unit = {
      val q: DBObject = "datetimeInterval" $lt datetime
      db(HOURLY_COLLECTION_NAME).remove(q)
    }

    def deleteDailyStatsBefore(datetime: Long): Unit = {
      val q: DBObject = "datetimeInterval" $lt datetime
      db(DAILY_COLLECTION_NAME).remove(q)
    }

    def saveToMinutely(doc: DaoObject): AnyRef = {
      db(MINUTELY_COLLECTION_NAME).insert(doc.asDbObject())
    }

    def saveToHourly(doc: DaoObject): AnyRef = {
      db(HOURLY_COLLECTION_NAME).insert(doc.asDbObject())
    }

    def saveToDaily(doc: DaoObject): AnyRef = {
      db(DAILY_COLLECTION_NAME).insert(doc.asDbObject())
    }

    def updateMinutely(sensorEventsStatistics: SensorEventsStatistics): Unit = {
      val query = MongoDBObject("_id" -> new ObjectId(sensorEventsStatistics.oid.get))
      val update = $set(
        "sensorId" -> sensorEventsStatistics.sensorId,
        "sensorEventsCount" -> sensorEventsStatistics.sensorEventsCount,
        "datetimeInterval" -> sensorEventsStatistics.datetimeInterval,
        "updatedAt" -> new Date()
      )
      db(MINUTELY_COLLECTION_NAME).update(query, update, upsert = false)
    }

    def updateHourly(sensorEventsStatistics: SensorEventsStatistics): Unit = {
      val query = MongoDBObject("_id" -> new ObjectId(sensorEventsStatistics.oid.get))
      val update = $set(
        "sensorId" -> sensorEventsStatistics.sensorId,
        "sensorEventsCount" -> sensorEventsStatistics.sensorEventsCount,
        "datetimeInterval" -> sensorEventsStatistics.datetimeInterval,
        "updatedAt" -> new Date()
      )
      db(HOURLY_COLLECTION_NAME).update(query, update, upsert = false)
    }


    def updateDaily(sensorEventsStatistics: SensorEventsStatistics): Unit = {
      val query = MongoDBObject("_id" -> new ObjectId(sensorEventsStatistics.oid.get))
      val update = $set(
        "sensorId" -> sensorEventsStatistics.sensorId,
        "sensorEventsCount" -> sensorEventsStatistics.sensorEventsCount,
        "datetimeInterval" -> sensorEventsStatistics.datetimeInterval,
        "updatedAt" -> new Date()
      )
      db(DAILY_COLLECTION_NAME).update(query, update, upsert = false)
    }


  }

}