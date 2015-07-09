package com.itylos.core.domain

import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.json4s.JsonAST.JObject

/**
 * Holds specific system's settings
 */
case class SystemSettings(var maxAlarmPasswordRetries: Int,
                          var maxSecondsToDisarm: Int,
                          var delayToArm: Int,
                          var playSoundsForSensorEvents: Boolean,
                          var playSoundsForTriggeredAlarm: Boolean,
                          var playSoundsForAlarmStatusUpdates: Boolean,
                          var accessToken: String)
  extends DaoObject with ParameterValidator {

  def this() {
    this(3, 15, 15,true,true,true,"accessToken_ChangeMe")
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      obj.getAsOrElse[Int]("maxAlarmPasswordRetries", -1),
      obj.getAsOrElse[Int]("maxSecondsToDisarm", -1),
      obj.getAsOrElse[Int]("delayToArm", -1),
      obj.getAsOrElse[Boolean]("playSoundsForSensorEvents", false),
      obj.getAsOrElse[Boolean]("playSoundsForTriggeredAlarm", false),
      obj.getAsOrElse[Boolean]("playSoundsForAlarmStatusUpdates", false),
      obj.getAsOrElse[String]("accessToken", "accessToken")
    )
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   */
  def fromJObject(data: JObject) {
    maxAlarmPasswordRetries = getParameter(data, "maxAlarmPasswordRetries").get.toInt
    maxSecondsToDisarm = getParameter(data, "maxSecondsToDisarm").get.toInt
    delayToArm = getParameter(data, "delayToArm").get.toInt
    accessToken = getParameter(data, "accessToken").get
    playSoundsForSensorEvents = getParameter(data, "playSoundsForSensorEvents").get.toBoolean
    playSoundsForTriggeredAlarm = getParameter(data, "playSoundsForTriggeredAlarm").get.toBoolean
    playSoundsForAlarmStatusUpdates = getParameter(data, "playSoundsForAlarmStatusUpdates").get.toBoolean
  }

  def fromMap(data: Map[String, Any]) {
    maxAlarmPasswordRetries = data.get("maxAlarmPasswordRetries").get.asInstanceOf[BigInt].toInt
    maxSecondsToDisarm = data.get("maxSecondsToDisarm").get.asInstanceOf[BigInt].toInt
    delayToArm = data.get("delayToArm").get.asInstanceOf[BigInt].toInt
    accessToken = data.get("accessToken").get.asInstanceOf[String]
    playSoundsForSensorEvents = data.get("playSoundsForSensorEvents").get.asInstanceOf[Boolean]
    playSoundsForTriggeredAlarm = data.get("playSoundsForTriggeredAlarm").get.asInstanceOf[Boolean]
    playSoundsForAlarmStatusUpdates = data.get("playSoundsForAlarmStatusUpdates").get.asInstanceOf[Boolean]
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += ("maxAlarmPasswordRetries" -> maxAlarmPasswordRetries)
    builder += ("maxSecondsToDisarm" -> maxSecondsToDisarm)
    builder += ("delayToArm" -> delayToArm)
    builder += ("accessToken" -> accessToken)
    builder += ("playSoundsForSensorEvents" -> playSoundsForSensorEvents)
    builder += ("playSoundsForTriggeredAlarm" -> playSoundsForTriggeredAlarm)
    builder += ("playSoundsForAlarmStatusUpdates" -> playSoundsForAlarmStatusUpdates)
    builder.result()
  }

}
