package com.itylos.core.domain

import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.json4s.JsonAST.JObject

/**
 * Holds data for a user
 */
case class User(var oid: Option[String],
                var name: String,
                var email: String,
                var webPassword: String,
                var alarmPassword: String,
                var dateRegistered: Long = new DateTime().getMillis,
                var isAdmin: Boolean = false) extends DaoObject with ParameterValidator {

  /**
   * Check if user's web passwords matches the given password
   * @param password the password to check against
   * @return true if they match
   */
  def webPasswordMatches(password: String): Boolean = webPassword == password


  def this() {
    this(None, "", "",  "", "", 0L, true)
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   * @param isIdRequired if the id should be present
   */
  def fromJObject(data: JObject, isIdRequired: Boolean) {
    oid = getParameter(data, "oid", isIdRequired)
    name = getParameter(data, "name").get
    email = getParameter(data, "email").get
    webPassword = getParameter(data, "webPassword").get
    alarmPassword = getParameter(data, "alarmPassword").get
    dateRegistered = new DateTime().getMillis
    isAdmin = getParameter(data, "isAdmin").get.toBoolean
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      Some(obj.get("_id").toString),
      obj.getAs[String]("name").get,
      obj.getAs[String]("email").get,
      obj.getAs[String]("webPassword").get,
      obj.getAs[String]("alarmPassword").get,
      obj.getAs[Long]("dateRegistered").get,
      obj.getAs[Boolean]("isAdmin").get
    )
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("name" -> name)
    builder += ("email" -> email)
    builder += ("webPassword" -> webPassword)
    builder += ("alarmPassword" -> alarmPassword)
    builder += ("dateRegistered" -> dateRegistered)
    builder += ("isAdmin" -> isAdmin)
    builder.result()
  }


}
