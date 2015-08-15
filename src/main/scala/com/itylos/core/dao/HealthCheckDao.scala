package com.itylos.core.dao

import com.itylos.core.domain.{HealthCheck, Zone}
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Dao for [[com.itylos.core.domain.HealthCheck]]
 */
trait HealthCheckComponent {
  val healthCheckDao: HealthCheckDao

  class HealthCheckDao extends CommonDao[HealthCheck] {
    val COLLECTION_NAME = "healthChecks"

    override def getCollectionName: String = COLLECTION_NAME


    /**
     * Remove all configured health checks urls
     */
    def removeAllHealthChecks(): Unit = {
      val q: DBObject = "lastTimeChecked" $gte -1
      db(getCollectionName).remove(q)
    }

    /**
     * Queries for all health checks
     */
    def getAllHealthChecks: List[HealthCheck] = {
      val data = listAll()
      if (data.isEmpty) return List()
      data.map(x => new HealthCheck(x)).toList
    }


    def update(healthCheck: HealthCheck): Unit = {
      val query = MongoDBObject("_id" -> new ObjectId(healthCheck.oid.get))
      val update = $set(
        "url" -> healthCheck.url,
        "lastCheckStatusCode" -> healthCheck.lastCheckStatusCode,
        "lastTimeChecked" -> healthCheck.lastTimeChecked,
        "checkInterval" -> healthCheck.checkInterval)
      db(getCollectionName).update(query, update, upsert = true)
    }

  }

}