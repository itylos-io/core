package com.itulos.controller.domain

import com.itulos.controller.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import org.joda.time.DateTime
import org.json4s.JsonAST.JObject

/**
 * Holds data for a sensor token
 */
case class SensorToken(var oid: Option[String],
                       var token: String,
                       var dateCreated: Long) extends DaoObject with ParameterValidator {

  def this() {
    this(None, "", 0L)
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   * @param isIdRequired if the id should be present
   */
  def fromJObject(data: JObject, isIdRequired: Boolean) {
    oid = getParameter(data, "oid", isIdRequired)
    token = getParameter(data, "token").get
    dateCreated = new DateTime().getMillis
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      Some(obj.get("_id").toString),
      obj.getAs[String]("token").get,
      obj.getAs[Long]("dateRegistered").get
    )
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("token" -> token)
    builder += ("dateRegistered" -> dateCreated)
    builder.result()
  }


}
