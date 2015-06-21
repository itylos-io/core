package com.itulos.controller.domain


import com.itulos.controller.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.json4s.JsonAST.JObject

/**
 * Holds data for a sensor event
 */
case class SensorEvent(var oid: Option[String],
                       var sensorId: String,
                       var status: SensorStatus,
                       var dateOfEvent: Long) extends DaoObject with ParameterValidator {

  def this() {
    this(None, "", OPEN, 0L)
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   * @param isIdRequired if the id should be present
   */
  def fromJObject(data: JObject, isIdRequired: Boolean) {
    oid = getParameter(data, "oid", isIdRequired)
    sensorId = getParameter(data, "sensorId").get
    status = SensorStatus.from(getParameter(data, "status").get)
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
      SensorStatus.from(obj.getAs[String]("status").get),
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
    builder += ("status" -> status.toString)
    builder += ("dateOfEvent" -> dateOfEvent)
    builder.result()
  }


}
