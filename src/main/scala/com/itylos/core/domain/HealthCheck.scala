package com.itylos.core.domain

import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Holds data for a URL that itylos should check for it's health
 */
case class HealthCheck(var oid: Option[String] = None,
                       var url: String,
                       var lastCheckStatusCode: Int,
                       var lastTimeChecked: Long,
                       var checkInterval: Int
                        ) extends DaoObject {


  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      Some(obj.get("_id").toString),
      obj.getAs[String]("url").get,
      obj.getAs[Int]("lastCheckStatusCode").get,
      obj.getAs[Long]("lastTimeChecked").get,
      obj.getAs[Int]("checkInterval").get
    )
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("url" -> url)
    builder += ("lastCheckStatusCode" -> lastCheckStatusCode)
    builder += ("lastTimeChecked" -> lastTimeChecked)
    builder += ("checkInterval" -> checkInterval)
    builder.result()
  }


}
