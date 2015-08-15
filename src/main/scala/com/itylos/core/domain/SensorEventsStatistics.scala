package com.itylos.core.domain

import java.util.Date

import com.mongodb.casbah.Imports
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Holds number of sensor events within 5 minutes
 */
case class SensorEventsStatistics(var oid: Option[String] = None,
                                  var sensorId: String,
                                  var sensorEventsCount: Int,
                                  var datetimeInterval: Long
                                   ) extends DaoObject {


  def incrementSensorCount(): Unit = {
    sensorEventsCount = sensorEventsCount + 1
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      Some(obj.get("_id").toString),
      obj.get("sensorId").toString,
      obj.get("sensorEventsCount").toString.toInt,
      obj.get("datetimeInterval").toString.toLong
    )
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("sensorId" -> sensorId)
    builder += ("sensorEventsCount" -> sensorEventsCount)
    builder += ("datetimeInterval" -> datetimeInterval)
    builder += ("updatedAt" -> new Date())
    builder.result()
  }


}
