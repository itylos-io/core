package com.itylos.core.domain

import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.json4s.JsonAST.JObject


case class EmailSettings(var isEnabled: Boolean = false,
                         var smtpStartTLSEnabled: Boolean,
                         var smtpHost: String,
                         var smtpUser: String,
                         var smtpPassword: String,
                         var smtpPort: Int,
                         var smtpAuth: Boolean,
                         var emailsToNotify: List[String]) extends DaoObject with ParameterValidator {

  def this() {
    this(false, true, "smtp.gmail.com", "email@gmail.com", "123", 587, true, List())
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   */
  def fromJObject(data: JObject) {
    isEnabled = getParameter(data, "isEnabled").get.toBoolean
    smtpStartTLSEnabled = getParameter(data, "smtpStartTLSEnabled").get.toBoolean
    smtpUser = getParameter(data, "smtpUser").get
    smtpPassword = getParameter(data, "smtpPassword").get
    smtpPort = getParameter(data, "smtpPort").get.toInt
    smtpAuth = getParameter(data, "smtpAuth").get.toBoolean
    emailsToNotify = getList(data, "emailsToNotify").get.asInstanceOf[List[String]]
  }

  def fromMap(data: Map[String, Any]) {
    isEnabled = data.get("isEnabled").get.asInstanceOf[Boolean]
    smtpStartTLSEnabled = data.get("smtpStartTLSEnabled").get.asInstanceOf[Boolean]
    smtpHost = data.get("smtpHost").get.asInstanceOf[String]
    smtpUser = data.get("smtpUser").get.asInstanceOf[String]
    smtpPassword = data.get("smtpPassword").get.asInstanceOf[String]
    smtpPort = data.get("smtpPort").get.asInstanceOf[BigInt].toInt
    smtpAuth = data.get("smtpAuth").get.asInstanceOf[Boolean]
    emailsToNotify = data.get("emailsToNotify").get.asInstanceOf[List[String]]
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: DBObject) {
    this(
      obj.getAsOrElse[Boolean]("isEnabled", false),
      obj.getAsOrElse[Boolean]("smtpStartTLSEnabled", true),
      obj.getAsOrElse[String]("smtpHost", "smtpHost"),
      obj.getAsOrElse[String]("smtpUser", "smtpUser"),
      obj.getAsOrElse[String]("smtpPassword", "smtpPassword"),
      obj.getAsOrElse[Int]("smtpPort", 999),
      obj.getAsOrElse[Boolean]("smtpAuth", true),
      obj.getAsOrElse[String]("emailsToNotify", "").split(",").toList
    )
    if (obj.getAsOrElse[String]("emailsToNotify", "") == "") emailsToNotify = List()
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += ("isEnabled" -> isEnabled)
    builder += ("smtpStartTLSEnabled" -> smtpStartTLSEnabled)
    builder += ("smtpHost" -> smtpHost)
    builder += ("smtpUser" -> smtpUser)
    builder += ("smtpPassword" -> smtpPassword)
    builder += ("smtpPort" -> smtpPort)
    builder += ("smtpAuth" -> smtpAuth)
    builder += ("emailsToNotify" -> emailsToNotify.mkString(","))
    builder.result()
  }


}
