package com.itylos.core.dao

import com.itylos.core.domain.KerberosEventImages
import com.mongodb.casbah.query.Imports._
import org.bson.types.ObjectId

/**
 * Dao for User
 */
trait KerberosEventImagesComponent {
  val kerberosEventImagesDao: KerberosEventImagesDao

  class KerberosEventImagesDao extends CommonDao[KerberosEventImages] {
    val COLLECTION_NAME = "kerberosEventImages"

    override def getCollectionName: String = COLLECTION_NAME

    def createIndex(): Unit ={
      val indexes = MongoDBObject("updatedAt" -> 1,"expireAfterSeconds"->3600)
      db(COLLECTION_NAME).ensureIndex(indexes)
    }

    /**
     * Get a List of [[com.itylos.core.domain.KerberosEventImages]] assocatied to a kerberos event
     * @param kerberosEventId the id of the kerberos event
     */
    def getImagesForKerberosEvent(kerberosEventId: String): Option[KerberosEventImages] = {
      val data = getByField("kerberosEventId", kerberosEventId)
      if (data != None) {
        return Some(new KerberosEventImages(data.get))
      }
      None
    }

    def update(kerberosEventImages: KerberosEventImages): Unit = {
      val query = MongoDBObject("_id" -> new ObjectId(kerberosEventImages.oid.get))
      val update = $set(
        "kerberosEventId" -> kerberosEventImages.kerberosEventId,
        "imagesUrls" -> kerberosEventImages.imagesUrls
      )
      db(getCollectionName).update(query, update, upsert = false)
    }

  }

}