package com.itylos.core.domain

import com.itylos.core.exception.{AlarmStatusEnum, DISARMED}
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime

/**
 * Holds data for an Alarm Status History
 */
case class AlarmStatusHistory(var oid: Option[String] = None,
                              var status: AlarmStatusEnum = DISARMED,
                              var zoneIds: List[String] = List(), // The zones that have been armed
                              var timeArmed: Long = 0L, // The time the alarm was armed
                              var timeDisArmed: Long = 0L, // The time the alarm was disarmed
                              var userIdArmed: String = "", // The user that armed the system
                              var userIdDisarmed: String = "", // The user that disarmed the system
                              var dateCreated: Long = new DateTime().getMillis
                               ) extends DaoObject {


  def this(alarmStatus: AlarmStatus) {
    this(
      None,
      alarmStatus.status,
      alarmStatus.zoneIds,
      alarmStatus.timeArmed,
      alarmStatus.timeDisArmed,
      alarmStatus.userIdArmed,
      alarmStatus.userIdDisarmed
    )
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      Some(obj.get("_id").toString),
      AlarmStatusEnum.from(obj.getAs[String]("status").get),
      obj.getAs[String]("zoneIds").get.split(",").toList,
      obj.getAs[Long]("timeArmed").get,
      obj.getAs[Long]("timeDisArmed").get,
      obj.getAs[String]("userIdArmed").get,
      obj.getAs[String]("userIdDisarmed").get
    )
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("status" -> status.toString)
    builder += ("zoneIds" -> zoneIds.mkString(","))
    builder += ("timeArmed" -> timeArmed)
    builder += ("timeDisArmed" -> timeDisArmed)
    builder += ("userIdArmed" -> userIdArmed)
    builder += ("userIdDisarmed" -> userIdDisarmed)
    builder += ("dateCreated" -> dateCreated)
    builder.result()
  }


}
