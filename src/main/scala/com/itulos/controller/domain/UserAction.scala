package com.itulos.controller.domain

import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Holds data for a user's action (login/logout)
 */
case class UserAction(userId: String,
                      date: Long,
                      action: UserActionType,
                      ip: String) extends DaoObject {

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += ("userId" -> userId)
    builder += ("date" -> date)
    builder += ("action" -> action.toString)
    builder += ("ip" -> ip)
    builder.result()
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      obj.getAs[String]("userId").get,
      obj.getAs[Long]("date").get,
      UserActionType.fromString(obj.getAs[String]("action").get),
      obj.getAs[String]("ip").get
    )
  }

}
