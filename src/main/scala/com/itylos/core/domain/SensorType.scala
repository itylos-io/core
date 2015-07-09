package com.itylos.core.domain

import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import spray.json.DefaultJsonProtocol

object SensorTypeJsonProtocol extends DefaultJsonProtocol {
  implicit val colorFormat = jsonFormat4(SensorType)
}

/**
 * Holds data for a specific sensor type
 */
case class SensorType(id: String,
                      name: String,
                      description: String,
                      isBatteryPowered: Boolean) extends DaoObject {

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += ("_id" -> id)
    builder += ("name" -> name)
    builder += ("description" -> description)
    builder += ("isBatteryPowered" -> isBatteryPowered)
    builder.result()


  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      obj.get("_id").toString,
      obj.getAs[String]("name").get,
      obj.getAs[String]("description").get,
      obj.getAs[Boolean]("isBatteryPowered").get
    )
  }

}
