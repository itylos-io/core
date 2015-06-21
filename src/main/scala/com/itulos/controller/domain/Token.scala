package com.itulos.controller.domain

import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Holds data for a user's token
 */
case class Token(userId: String,
                 token: String,
                 expireTime: Long) extends DaoObject {

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += ("userId" -> userId)
    builder += ("token" -> token)
    builder += ("expireTime" -> expireTime)
    builder.result()
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      obj.getAs[String]("userId").get,
      obj.getAs[String]("token").get,
      obj.getAs[Long]("expireTime").get
    )
  }

}
