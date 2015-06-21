package com.itulos.controller.domain

import com.itulos.controller.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.json4s.JsonAST.JObject

/**
 * Holds data for a zone's status
 */
case class ZoneStatus(var zoneId: String,
                      var status: ZoneStatusEnum=ENABLED,
                      var dateUpdated: Long=new DateTime().getMillis) extends DaoObject with ParameterValidator {

  def this() {
    this("", ENABLED)
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   */
  def fromJObject(data: JObject, user: User) {
    zoneId = getParameter(data, "zoneId").get
    status = ZoneStatusEnum.from(getParameter(data, "status").get)
    dateUpdated = new DateTime().getMillis
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += ("zoneId" -> zoneId)
    builder += ("status" -> status.toString)
    builder += ("dateUpdated" -> dateUpdated)
    builder.result()
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      obj.getAs[String]("zoneId").get,
      ZoneStatusEnum.from(obj.getAs[String]("status").get),
      obj.getAs[Long]("dateUpdated").get
    )
  }

}
