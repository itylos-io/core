package com.itylos.core.domain

import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.json4s.JsonAST.JObject


case class NexmoSettings(var isEnabled: Boolean = false,
                         var nexmoKey: String,
                         var nexmoSecret: String,
                         var mobilesToNotify: List[String],
                         var nexmoEndpoint: String = "https://rest.nexmo.com/sms/json") extends DaoObject with ParameterValidator {

  def this() {
    this(false, "nexmoKey", "nexmoSecret", List())
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   */
  def fromJObject(data: JObject) {
    isEnabled = getParameter(data, "isEnabled").get.toBoolean
    nexmoKey = getParameter(data, "nexmoKey").get
    nexmoSecret = getParameter(data, "nexmoSecret").get
    mobilesToNotify = getList(data, "mobilesToNotify").get.asInstanceOf[List[String]]
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
      obj.getAsOrElse[String]("mobilesToNotify", "").split(",").toList,
      obj.getAsOrElse[String]("nexmoEndpoint", "nexmoEndpoint")
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
    builder += ("nexmoEndpoint" -> nexmoEndpoint)
    builder.result()
  }


}
