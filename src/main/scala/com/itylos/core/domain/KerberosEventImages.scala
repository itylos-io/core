package com.itylos.core.domain

import java.util.Date

import com.itylos.core.rest.ParameterValidator
import com.mongodb.casbah.Imports
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject

case class KerberosEventImages(var oid: Option[String],
                               var kerberosEventId: String,
                               var imagesUrls: List[String]) extends DaoObject with ParameterValidator {

  def this() {
    this(None,"id", List())
  }

  def addImageUrlToEvents(imagesUrl: String): Unit = {
    if (imagesUrls.contains(imagesUrl)) return
    imagesUrls = List(imagesUrl) ++ imagesUrls
  }



  /**
   * Constructor with a DBObject
   * @param obj the DBObject from which to retrieve data
   */
  def this(obj: DBObject) {
    this(
      Some(obj.get("_id").toString),
      obj.getAs[String]("kerberosEventId").get,
      obj.getAs[List[String]]("imagesUrls").get
    )
  }

  /**
   * @return a representation of this object as Db Object
   */
  override def asDbObject(): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    if (oid != None) builder += ("_id" -> oid)
    builder += ("kerberosEventId" -> kerberosEventId)
    builder += ("imagesUrls" -> imagesUrls)
    builder += ("updatedAt" -> new Date())
    builder.result()
  }


}
