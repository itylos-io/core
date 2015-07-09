package com.itylos.core.domain

import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.json4s.JsonAST.JObject

/**
 * Settings related to PushBullet configuration
 * @param notifyForSensorEvents whether to notify for sensor events via PushBullet for sensor events
 * @param notifyForAlarmsStatusUpdates  whether to notify for alarm status updates via PushBullet for sensor events
 * @param notifyForAlarms whether to notify alarm triggered via PushBullet for enabled alarms
 * @param isEnabled indicates whether PushBullet notifications are enabled or not
 * @param accessToken the user's PushBullet access token
 * @param devices the list of [[com.itylos.core.domain.PushBulletDevice]] associated to the account
 */
case class PushBulletSettings(var isEnabled: Boolean = false,
                              var notifyForSensorEvents: Boolean = true,
                              var notifyForAlarms: Boolean = true,
                              var notifyForAlarmsStatusUpdates: Boolean = true,
                              var accessToken: String,
                              var devices: List[PushBulletDevice],
                              var pushBulletEndpoint: String = "https://api.pushbullet.com/v2/pushes"
                               ) extends DaoObject with ParameterValidator {

  def this() {
    this(false, true, true, true, "accessToken", List())
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   */
  def fromJObject(data: JObject) {
    isEnabled = getParameter(data, "isEnabled").get.toBoolean
    notifyForSensorEvents = getParameter(data, "notifyForSensorEvents").get.toBoolean
    notifyForAlarms = getParameter(data, "notifyForAlarms").get.toBoolean
    notifyForAlarmsStatusUpdates = getParameter(data, "notifyForAlarmsStatusUpdates").get.toBoolean
    accessToken = getParameter(data, "accessToken").get
    devices = getList(data, "devices").get.asInstanceOf[List[PushBulletDevice]]
  }

  def fromMap(data: Map[String, Any]) {
    isEnabled = data.get("isEnabled").get.asInstanceOf[Boolean]
    notifyForSensorEvents = data.get("notifyForSensorEvents").get.asInstanceOf[Boolean]
    notifyForAlarms = data.get("notifyForAlarms").get.asInstanceOf[Boolean]
    notifyForAlarmsStatusUpdates = data.get("notifyForAlarmsStatusUpdates").get.asInstanceOf[Boolean]
    accessToken = data.get("accessToken").get.asInstanceOf[String]
    devices = data.get("devices").get.asInstanceOf[List[PushBulletDevice]]
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: DBObject) {
    this(
      obj.getAsOrElse[Boolean]("isEnabled", false),
      obj.getAsOrElse[Boolean]("notifyForSensorEvents", false),
      obj.getAsOrElse[Boolean]("notifyForAlarms", false),
      obj.getAsOrElse[Boolean]("notifyForAlarmsStatusUpdates", false),
      obj.getAsOrElse[String]("accessToken", "accessToken"),
      List(),
      obj.getAsOrElse[String]("pushBulletEndpoint", "pushBulletEndpoint")
    )

    devices = for (device <- obj.getAsOrElse[List[BasicDBObject]]("devices", List())) yield {
      new PushBulletDevice(
        device.getAsOrElse[Boolean]("isEnabled", false),
        device.getAsOrElse[String]("iden", "iden"),
        device.getAsOrElse[String]("deviceName", "nickname")
      )
    }
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += ("isEnabled" -> isEnabled)
    builder += ("notifyForSensorEvents" -> notifyForSensorEvents)
    builder += ("notifyForAlarms" -> notifyForAlarms)
    builder += ("notifyForAlarmsStatusUpdates" -> notifyForAlarmsStatusUpdates)
    builder += ("accessToken" -> accessToken)
    builder += ("devices" -> devices)
    builder += ("pushBulletEndpoint" -> pushBulletEndpoint)
    builder.result()
  }

}



