package com.itylos.core.dao

import com.itylos.core.domain.ZoneStatus
import com.mongodb.casbah.query.Imports._
import org.joda.time.DateTime

/**
 * Dao for ZoneStatus
 */
trait ZoneStatusComponent {
  val zoneStatusDao: ZoneStatusDao

  class ZoneStatusDao extends CommonDao[ZoneStatus] {
    val COLLECTION_NAME = "zoneStatus"

    override def getCollectionName: String = COLLECTION_NAME

    /**
     * Update ZoneStatus object. Creates new entry if ZoneStatus with zone id is not present
     * @param zoneStatus the ZoneStatus to get data from
     */
    def update(zoneStatus: ZoneStatus): Unit = {
      val query = MongoDBObject("zoneId" -> zoneStatus.zoneId)
      val update = $set(
        "status" -> zoneStatus.status.toString,
        "dateUpdated" -> new DateTime().getMillis
      )
      db(getCollectionName).update(query, update, upsert = true)
    }

    /**
     * List all ZoneStatus objects
     * @return the ZoneStatus objects if any
     */
    def getAllZonesStatus: List[ZoneStatus] = {
      val data = listAll()
      if (data.isEmpty) return List()
      data.map(x => new ZoneStatus(x)).toList
    }

    /**
     * Query for zone status data based on zone's id
     * @param zoneId the object id of the zone
     * @return the ZoneStatus if any
     */
    def getZoneStatusByZoneId(zoneId: String): Option[ZoneStatus] = {
      val data = getByField("zoneId", zoneId)
      if (data != None) {
        return Some(new ZoneStatus(data.get))
      }
      None
    }

    /**
     * Delete a zone's status
     * @param zoneId the id of the zone to delete status for
     */
    def deleteZoneStatusByZoneId(zoneId: String): Unit = {
      val data = getByField("zoneId", zoneId)
      if (data != None) db(getCollectionName).remove(data.get)
    }

  }

}