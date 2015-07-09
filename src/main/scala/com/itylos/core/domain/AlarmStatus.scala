package com.itylos.core.domain

import com.itylos.core.exception.{AlarmStatusEnum, DISARMED}
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime

/**
 * Holds data for an Enabled Alarm
 */
case class AlarmStatus(var oid: Option[String] = None,
                       var status: AlarmStatusEnum = DISARMED,
                       var zoneIds: List[String] = List(), // The zones that have been violated
                       var sensorOIds: List[String] = List(), // The sensors that have been violated
                       var falseEnteredPasswords: Int = 0,
                       var emailNotificationsSent: Boolean = false,
                       var smsSent: Boolean = false,
                       var pushBulletNotificationsSent: Boolean = false,
                       var violationTime: Long = -1L,
                       var timeArmed: Long = new DateTime().getMillis, // The time the alarm was armed
                       var timeDisArmed: Long = new DateTime().getMillis, // The time the alarm was disarmed
                       var userIdArmed: String = "", // The user that armed the system
                       var userIdDisarmed: String = "" // The user that disarmed the system
                        ) extends DaoObject {

  def addNewSensor(sensorOId: String): Unit = {
    if (sensorOIds.contains(sensorOId)) return
    sensorOIds = List(sensorOId) ++ sensorOIds
  }

  def addNewZone(zoneId: String): Unit = {
    if (zoneIds.contains(zoneId)) return
    zoneIds = List(zoneId) ++ zoneIds
  }

  def resetAlarmStatus(): Unit = {
    falseEnteredPasswords = 0
    zoneIds = List()
    sensorOIds = List()
    emailNotificationsSent = false
    pushBulletNotificationsSent = false
    smsSent = false
    violationTime = -1
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
      obj.getAs[String]("sensorOIds").get.split(",").toList,
      obj.getAs[Int]("falseEnteredPasswords").get,
      obj.getAs[Boolean]("emailNotificationsSent").get,
      obj.getAs[Boolean]("smsSent").get,
      obj.getAs[Boolean]("pushBulletNotificationsSent").get,
      obj.getAs[Long]("violationTime").get,
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
    builder += ("sensorOIds" -> sensorOIds.mkString(","))
    builder += ("falseEnteredPasswords" -> falseEnteredPasswords)
    builder += ("emailNotificationsSent" -> emailNotificationsSent)
    builder += ("pushBulletNotificationsSent" -> pushBulletNotificationsSent)
    builder += ("smsSent" -> smsSent)
    builder += ("violationTime" -> violationTime)
    builder += ("timeArmed" -> timeArmed)
    builder += ("timeDisArmed" -> timeDisArmed)
    builder += ("userIdArmed" -> userIdArmed)
    builder += ("userIdDisarmed" -> userIdDisarmed)
    builder.result()
  }


}
