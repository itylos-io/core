package com.itylos.core.domain


import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.json4s.JsonAST.JObject

/**
 * Holds data for a sensor event
 * status is either 0 when closed,movement stopped etc
 * or 1 when open, movement detected etc..
 */
case class SensorEvent(var oid: Option[String],
                       var sensorId: String,
                       var status: Int, // 0 or 1
                       var batteryLevel: Int,
                       var kerberosEventId: Option[String], //
                       var dateOfEvent: Long) extends DaoObject with ParameterValidator {

  def this() {
    this(None, "", 1, 100, None, 0L)
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   * @param isIdRequired if the id should be present
   */
  def fromJObject(data: JObject, isIdRequired: Boolean) {
    oid = getParameter(data, "oid", isIdRequired)
    sensorId = getParameter(data, "sensorId").get
    batteryLevel = getParameter(data, "batteryLevel", isRequired = false).getOrElse(-1).toString.toInt
    status = getParameter(data, "status").get.toInt
    kerberosEventId = getParameter(data, "kerberosEventId", isRequired = false)
    dateOfEvent = new DateTime().getMillis
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      Some(obj.get("_id").toString),
      obj.getAs[String]("sensorId").get,
      obj.getAs[Int]("status").get,
      obj.getAs[Int]("batteryLevel").get,
      obj.getAs[String]("kerberosEventId"),
      obj.getAs[Long]("dateOfEvent").get
    )
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("sensorId" -> sensorId)
    builder += ("status" -> status)
    builder += ("batteryLevel" -> batteryLevel)
    if (kerberosEventId != None) builder += ("kerberosEventId" -> kerberosEventId.get)
    builder += ("dateOfEvent" -> dateOfEvent)
    builder.result()
  }


}
