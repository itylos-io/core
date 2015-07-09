package com.itylos.core.domain

import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.json4s.JsonAST.JObject

/**
 * Holds data for a sensor
 */
case class Sensor(var oid: Option[String],
                  var sensorId: String,
                  var name: String,
                  var description: String,
                  var location: String,
                  var sensorTypeId: String,
                  var isActive: Boolean = true,
                  var dateRegistered: Long) extends DaoObject with ParameterValidator {

  def this() {
    this(None, "", "", "", "", "", false, 0L)
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   * @param isIdRequired if the id should be present
   */
  def fromJObject(data: JObject, isIdRequired: Boolean) {
    oid = getParameter(data, "oid", isIdRequired)
    sensorId = getParameter(data, "sensorId").get
    name = getParameter(data, "name").get
    description = getParameter(data, "description").get
    location = getParameter(data, "location").get
    sensorTypeId = getParameter(data, "sensorTypeId").get
    isActive = getParameter(data, "isActive").get.toBoolean
    dateRegistered = new DateTime().getMillis
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      Some(obj.get("_id").toString),
      obj.getAs[String]("sensorId").get,
      obj.getAs[String]("name").get,
      obj.getAs[String]("description").get,
      obj.getAs[String]("location").get,
      obj.getAs[String]("sensorTypeId").get,
      obj.getAs[Boolean]("isActive").get,
      obj.getAs[Long]("dateRegistered").get
    )
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("sensorId" -> sensorId)
    builder += ("name" -> name)
    builder += ("description" -> description)
    builder += ("location" -> location)
    builder += ("sensorTypeId" -> sensorTypeId)
    builder += ("isActive" -> isActive)
    builder += ("dateRegistered" -> dateRegistered)
    builder.result()
  }


}
