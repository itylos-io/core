package com.itulos.controller.dao

import com.itulos.controller.domain.Zone
import com.itulos.controller.exception.ZoneDoesNotExistException
import com.mongodb.casbah.query.Imports._
import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
 * Dao for Zone
 */
trait ZoneComponent {
  val zoneDao: ZoneDao

  class ZoneDao extends CommonDao[Zone] {
    val COLLECTION_NAME = "zones"

    override def getCollectionName: String = COLLECTION_NAME

    /**
     * Checks if zones exists. Filters on object id of zones (oid)
     * @throws ZoneDoesNotExistException if oid does not correspond to a zone
     * @param zoneOIds the object ids of the zones to check
     */
    def checkZonesExistence(zoneOIds: List[String]): Unit = {
      zoneOIds.foreach(zoneOId => {
        if (getZoneByObjectId(zoneOId) == None) {
          throw new ZoneDoesNotExistException(zoneOId)
        }
      })
    }

    /**
     * Query for all zones associated to a sensor
     * @param sensorOId the object id of the sensor
     * @return a list of zones if any
     */
    def getZonesForSensorOid(sensorOId: String): List[Zone] = {
      val data = getAllZones
      data.filter(zone=>zone.sensorOIds.contains(sensorOId))
    }

    /**
     * Query for zone data based on oid
     * @param oid the object id of the zone
     * @return the Zone if any
     */
    def getZoneByObjectId(oid: String): Option[Zone] = {
      val data = getByIDField(oid)
      if (data != None) {
        return Some(new Zone(data.get))
      }
      None
    }

    /**
     * Delete zone data based on oid
     * @param oid the object id of the zone to delete
     */
    def deleteZoneByObjectId(oid: String) = {
      val data = getByIDField(oid)
      if (data != None) db(getCollectionName).remove(data.get)
    }

    /**
     * Queries for all Zones
     * @return the Zones
     */
    def getAllZones: List[Zone] = {
      val data = listAll()
      if (data.isEmpty) return List()
      data.map(x => new Zone(x)).toList
    }

    /**
     * Update a Zone's data
     * @param zone the updated zone.
     */
    def update(zone: Zone): Unit = {

      val query = MongoDBObject("_id" -> new ObjectId(zone.oid.get))
      val update = $set(
        "name" -> zone.name,
        "description" -> zone.description,
        "sensorOIds" -> zone.sensorOIds.mkString(","),
        "dateRegistered" -> new DateTime().getMillis
      )
      db(getCollectionName).update(query, update, upsert = false)
    }


  }

}