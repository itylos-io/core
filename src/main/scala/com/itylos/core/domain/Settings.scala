package com.itylos.core.domain

import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.json4s.JsonAST.JObject

/**
 * Holds all system's settings.
 * Each of the "sub-settings" is an embedded document to this document
 */
case class Settings(var oid: Option[String] = None,
                    var systemSettings: SystemSettings = new SystemSettings(),
                    var emailSettings: EmailSettings = new EmailSettings(),
                    var nexmoSettings: NexmoSettings = new NexmoSettings(),
                    var webHookSettings: WebHookSettings = new WebHookSettings(),
                    var pushBulletSettings: PushBulletSettings = new PushBulletSettings(),
                    var kerberosSettings: KerberosSettings = new KerberosSettings())
  extends DaoObject with ParameterValidator {

  /**
   * Constructor with a JObject
   * @param data the JObject
   */
  def fromJObject(data: JObject) {
    val emailSettingsObject = new EmailSettings()
    val nexmoSettingsObject = new NexmoSettings()
    val systemSettingsObject = new SystemSettings()
    val pushBulletSettingsObject = new PushBulletSettings()
    val webHookSettingsObject = new WebHookSettings()
    val kerberosSettingsObject = new KerberosSettings()
    emailSettingsObject.fromMap(data.values.get("emailSettings").get.asInstanceOf[Map[String, AnyRef]])
    nexmoSettingsObject.fromMap(data.values.get("nexmoSettings").get.asInstanceOf[Map[String, AnyRef]])
    systemSettingsObject.fromMap(data.values.get("systemSettings").get.asInstanceOf[Map[String, AnyRef]])
    pushBulletSettingsObject.fromMap(data.values.get("pushBulletSettings").get.asInstanceOf[Map[String, AnyRef]])
    pushBulletSettingsObject.fromMap(data.values.get("pushBulletSettings").get.asInstanceOf[Map[String, AnyRef]])
    webHookSettingsObject.fromMap(data.values.get("webHookSettings").get.asInstanceOf[Map[String, AnyRef]])
    kerberosSettingsObject.fromMap(data.values.get("kerberosSettings").get.asInstanceOf[Map[String, AnyRef]])
    emailSettings = emailSettingsObject
    nexmoSettings = nexmoSettingsObject
    systemSettings = systemSettingsObject
    pushBulletSettings = pushBulletSettingsObject
    webHookSettings = webHookSettingsObject
    kerberosSettings = kerberosSettingsObject
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: Imports.DBObject) {
    this(
      Some(obj.get("_id").toString),
      new SystemSettings(obj.getAs[DBObject]("systemSettings").get),
      new EmailSettings(obj.getAs[DBObject]("emailSettings").get),
      new NexmoSettings(obj.getAs[DBObject]("nexmoSettings").get),
      new WebHookSettings(obj.getAs[DBObject]("webHookSettings").get),
      new PushBulletSettings(obj.getAs[DBObject]("pushBulletSettings").get),
      new KerberosSettings(obj.getAs[DBObject]("kerberosSettings").get)
    )
  }


  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("emailSettings" -> emailSettings.asDbObject())
    builder += ("nexmoSettings" -> nexmoSettings.asDbObject())
    builder += ("systemSettings" -> systemSettings.asDbObject())
    builder += ("pushBulletSettings" -> pushBulletSettings.asDbObject())
    builder += ("kerberosSettings" -> kerberosSettings.asDbObject())
    builder += ("webHookSettings" -> webHookSettings.asDbObject())
    builder.result()
  }

}





