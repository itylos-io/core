package com.itulos.controller.dao

import com.itulos.controller.domain.DaoObject
import com.itulos.controller.exception.InvalidObjectIdException
import com.mongodb.DBObject
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId

abstract class CommonDao[T](val db: com.mongodb.casbah.MongoDB = MongoFactory.mongoClient) {

  /**
   * @return the collection name to store data
   */
  def getCollectionName: String

  /**
   * Save document to collection
   * @param doc the document to persist
   */
  def save(doc: DaoObject) {
    db(getCollectionName).insert(doc.asDbObject())
  }

  /**
   * Drops collection
   */
  def drop(): Unit ={
    db(getCollectionName).drop()
  }

  /**
   * Query for the only entry
   * @return the only entry if any
   */
  protected def getOne: Option[MongoCollection#T] ={
   db(getCollectionName).findOne()
  }

  /**
   * List all documents in collection
   * @return all documents
   * @param orderBy on which field to orderBy if any
   * @param asc true for ascending else descending
   * @param offset how many entries to skip
   */
  protected def listAll(orderBy: Option[String] = None, asc: Option[Boolean] = None,
                        limit: Int = 100, offset: Int = 0): MongoCollection#CursorType = {
    if (orderBy != None) {
      val order = if (asc.get) 1 else -1
      val orderQ = MongoDBObject(orderBy.get -> order)
      db(getCollectionName).find().sort(orderQ).skip(offset).limit(limit)
    } else {
      db(getCollectionName).find().skip(offset).limit(limit)
    }
  }

  /**
   * List all documents in collection using a filter
   * @param field the field to filter
   * @param value the value of the filter
   * @param limit how many to return
   * @param orderBy on which field to orderBy
   * @param asc true for ascending else descending
   * @param offset how many entries to skip
   * @return all documents
   */
  protected def listAllByFilter(orderBy: Option[String], asc: Option[Boolean], field: String,
                                value: AnyRef, limit: Int = 100, offset: Int = 0): MongoCollection#CursorType = {
    val q: DBObject = MongoDBObject(field -> value)

    if (orderBy != None) {
      val order = if (asc.get) 1 else -1
      val orderQ = MongoDBObject(orderBy.get -> order)
      db(getCollectionName).find(q).sort(orderQ).skip(offset).limit(limit)
    }
    db(getCollectionName).find(q).skip(offset).limit(limit)
  }

  /**
   * Get a document by filtering one field
   * @param field the field to filter
   * @param value the value of the filter
   * @return the document if found else None
   */
  protected def getByField(field: String, value: AnyRef): Option[MongoCollection#T] = {
    val q: DBObject = MongoDBObject(field -> value)
    db(getCollectionName).findOne(q)
  }

  /**
   * Get a document by filtering one id field
   * @param id the id of the document
   * @return the document if found else None
   */
  protected def getByIDField(id: String): Option[MongoCollection#T] = {
    try {
      if (id.trim.isEmpty) return None
      val objectId: ObjectId = new ObjectId(id)
      val q: DBObject = MongoDBObject("_id" -> objectId)
      db(getCollectionName).findOne(q)
    } catch {
      case e: Exception => throw new InvalidObjectIdException(id)
    }
  }


  /**
   * Remove a document by filtering one field
   * @param field the field to filter
   * @param value the value of the filter
   */
  protected def removeByField(field: String, value: AnyRef): Unit = {
    val q: DBObject = MongoDBObject(field -> value)
    db(getCollectionName).findAndRemove(q)

  }
}
