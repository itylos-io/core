package com.itulos.controller.domain

import com.itulos.controller.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.json4s.JsonAST.JObject

/**
 * Holds system's settings
 * @param maxAlarmPasswordRetries how many false retries should be allowed before triggering alarm
 * @param maxSecondsToDisarm how many seconds to wait to disarm zone before triggering alarm
 * @param emailSettings email settings
 */
case class SystemSettings(var oid: Option[String] = None,
                          var maxAlarmPasswordRetries: Int = 3,
                          var maxSecondsToDisarm: Int = 15,
                          var secondsToCompletelyArm:Int = 15,
                          var emailSettings: EmailSettings = new EmailSettings(),
                          var nexmoSettings: NexmoSettings = new NexmoSettings())
  extends DaoObject with ParameterValidator {

  /**
   * Constructor with a JObject
   * @param data the JObject
   */
  def fromJObject(data: JObject) {
    val emailSettingsObject = new EmailSettings()
    val nexmoSettingsObject = new NexmoSettings()
    maxAlarmPasswordRetries = getParameter(data, "maxAlarmPasswordRetries").get.toInt
    maxSecondsToDisarm = getParameter(data, "maxSecondsToDisarm").get.toInt
    secondsToCompletelyArm = getParameter(data, "secondsToCompletelyArm").get.toInt
    emailSettingsObject.fromMap(data.values.get("emailSettings").get.asInstanceOf[Map[String, AnyRef]])
    nexmoSettingsObject.fromMap(data.values.get("nexmoSettings").get.asInstanceOf[Map[String, AnyRef]])
    emailSettings = emailSettingsObject
    nexmoSettings = nexmoSettingsObject
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      Some(obj.get("_id").toString),
      obj.getAs[Int]("maxAlarmPasswordRetries").get,
      obj.getAs[Int]("maxSecondsToDisarm").get,
      obj.getAs[Int]("secondsToCompletelyArm").get,
      new EmailSettings(obj.getAs[DBObject]("emailSettings").get),
      new NexmoSettings(obj.getAs[DBObject]("nexmoSettings").get)
    )
  }


  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("maxAlarmPasswordRetries" -> maxAlarmPasswordRetries)
    builder += ("maxSecondsToDisarm" -> maxSecondsToDisarm)
    builder += ("secondsToCompletelyArm" -> secondsToCompletelyArm)
    builder += ("emailSettings" -> emailSettings.asDbObject())
    builder += ("nexmoSettings" -> nexmoSettings.asDbObject())
    builder.result()
  }

}


case class NexmoSettings(var isEnabled: Boolean = false,
                         var nexmoKey: String,
                         var nexmoSecret: String,
                         var mobilesToNotify: List[String]) extends DaoObject with ParameterValidator {

  def this() {
    this(false, "nexmoKey", "nexmoSecret", List())
  }

  def fromMap(data: Map[String, Any]) {
    isEnabled = data.get("isEnabled").get.asInstanceOf[Boolean]
    nexmoKey = data.get("nexmoKey").get.asInstanceOf[String]
    nexmoSecret = data.get("nexmoSecret").get.asInstanceOf[String]
    mobilesToNotify = data.get("mobilesToNotify").get.asInstanceOf[List[String]]
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: DBObject) {
    this(
      obj.getAsOrElse[Boolean]("isEnabled", false),
      obj.getAsOrElse[String]("nexmoKey", "nexmoKey"),
      obj.getAsOrElse[String]("nexmoSecret", "nexmoSecret"),
      obj.getAsOrElse[String]("mobilesToNotify", "").split(",").toList
    )
    if (obj.getAsOrElse[String]("mobilesToNotify", "") == "") mobilesToNotify = List()
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += ("isEnabled" -> isEnabled)
    builder += ("nexmoKey" -> nexmoKey)
    builder += ("nexmoSecret" -> nexmoSecret)
    builder += ("mobilesToNotify" -> mobilesToNotify.mkString(","))
    builder.result()
  }


}

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
