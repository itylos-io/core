package com.itylos.core.domain

import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.json4s.JsonAST.JObject

/**
 * Holds data for a zone
 */
case class Zone(var oid: Option[String],
                var name: String,
                var description: String,
                var sensorOIds: List[String],
                var dateCreated: Long) extends DaoObject with ParameterValidator {


  /**
   * Default no args constructor
   */
  def this() {
    this(None, "", "", List(), 0L)
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   * @param isIdRequired if the id should be present
   */
  def fromJObject(data: JObject, isIdRequired: Boolean) {
    oid = getParameter(data, "oid", isIdRequired)
    name = getParameter(data, "name").get
    description = getParameter(data, "description").get
    sensorOIds = getList(data, "sensorOIds").get.asInstanceOf[List[String]]
    dateCreated = new DateTime().getMillis
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      Some(obj.get("_id").toString),
      obj.getAs[String]("name").get,
      obj.getAs[String]("description").get,
      obj.getAs[String]("sensorOIds").map(_.split(",").toList.filter(p=>p.trim!="")).getOrElse(List()),
      obj.getAs[Long]("dateCreated").get
    )
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("name" -> name)
    builder += ("description" -> description)
    builder += ("sensorOIds" -> sensorOIds.mkString(","))
    builder += ("dateCreated" -> dateCreated)
    builder.result()
  }


}
