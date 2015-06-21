package com.itulos.controller.dao

import com.itulos.controller.domain.SensorEvent
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Dao for SensorEvent
 */
trait SensorEventComponent {
  val sensorEventDao: SensorEventDao

  class SensorEventDao extends CommonDao[SensorEvent] {
    val COLLECTION_NAME = "sensorEvents"
    val DATE_SORT_FIELD = Some("dateOfEvent")
    val DATE_SORT_ASC = Some(false)

    override def getCollectionName: String = COLLECTION_NAME

    /**
     * Get sensor events
     * @param sensorId the id of the sensor to filter if any
     * @param limit how many to return
     * @param offset how many to skip
     * @return the filtered SensorEvent if any
     */
    def getSensorEvents(sensorId: Option[String], limit: Int, offset: Int): List[SensorEvent] = {
      val data = if (sensorId == None) listAll(DATE_SORT_FIELD, DATE_SORT_ASC, limit, offset)
      else listAllByFilter(DATE_SORT_FIELD, DATE_SORT_ASC, "sensorId", sensorId, limit, offset)
      if (data.isEmpty) List() else data.map(x => new SensorEvent(x)).toList
    }

    /**
     * Get latest event for sensor
     * @param sensorId the id of the sensor to get latest event
     * @return the latest event if any
     */
    def getLatestSensorEvent(sensorId: String): Option[SensorEvent] = {
      val filterQ = MongoDBObject("sensorId" -> sensorId)
      val orderQ = MongoDBObject(DATE_SORT_FIELD.get -> -1)
      val data = db(getCollectionName).find(filterQ).sort(orderQ).limit(1)
      if (data.isEmpty) None else Some(new SensorEvent(data.one()))
    }

    /**
     * Remove events for a sensor
     * @param sensorId the id of the sensor to remove events for
     */
    def removeEventsForSensor(sensorId:String): Unit ={
      val q: DBObject =  MongoDBObject("sensorId" -> sensorId)
      val data =  db(getCollectionName).find(q)
       if (data.nonEmpty) data.foreach(d=>db(getCollectionName).remove(d))
    }

  }

}