package com.itylos.core.domain

import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import org.json4s.JsonAST.JObject

/**
 * Settings related to Kerberos.io configuration
 * @param kerberosInstances the list of [[com.itylos.core.domain.KerberosInstance]]
 */
case class KerberosSettings(var isEnabled: Boolean = false,
                            var kerberosInstances: List[KerberosInstance],
                            kerberosConditionUpdateEnpoint: String = "/api/v1/condition",
                            kerberosInstanceNameEnpoint: String = "/api/v1/instance_name"
                             ) extends DaoObject with ParameterValidator {

  def this() {
    this(false, List())
  }

  /**
   * Constructor with a JObject
   * @param data the JObject
   */
  def fromJObject(data: JObject) {
    isEnabled = getParameter(data, "isEnabled").get.toBoolean
    kerberosInstances = getList(data, "instances").get.asInstanceOf[List[KerberosInstance]]
  }

  def fromMap(data: Map[String, Any]) {
    isEnabled = data.get("isEnabled").get.asInstanceOf[Boolean]
    kerberosInstances = data.get("kerberosInstances").get.asInstanceOf[List[KerberosInstance]]
  }

  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: DBObject) {
    this(
      obj.getAsOrElse[Boolean]("isEnabled", false),
      List()
    )

    kerberosInstances = for (device <- obj.getAsOrElse[List[BasicDBObject]]("kerberosInstances", List())) yield {
      new KerberosInstance(
        device.getAsOrElse[String]("instanceName", "instanceName"),
        device.getAsOrElse[String]("ip", "ip"),
        device.getAsOrElse[String]("username", "username"),
        device.getAsOrElse[String]("password", "password")
      )
    }

  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += ("isEnabled" -> isEnabled)
    builder += ("kerberosInstances" -> kerberosInstances)
    builder += ("kerberosConditionUpdateEnpoint" -> kerberosConditionUpdateEnpoint)
    builder += ("kerberosInstanceNameEnpoint" -> kerberosInstanceNameEnpoint)


    try {
      // HACK TODO FIX THIS! TODO
      val instances = for (instance <- kerberosInstances) yield {
        Map("instanceName" -> instance.instanceName,
          "ip" -> instance.ip,
          "username" -> instance.username,
          "password" -> instance.password)
      }
      builder += ("kerberosInstances" -> instances)
    } catch {
      case e: Exception => builder += ("kerberosInstances" -> kerberosInstances)
    }


    builder.result()
  }

}



