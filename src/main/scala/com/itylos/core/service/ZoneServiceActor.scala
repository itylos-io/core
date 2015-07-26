package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao.{SensorComponent, SensorTypeComponent, ZoneComponent, ZoneStatusComponent}
import com.itylos.core.domain.{ENABLED, Zone, ZoneStatus}
import com.itylos.core.rest.dto.{SensorDto, ZoneDto}
import com.itylos.core.service.protocol._

/**
 * Companion object to properly start [[com.itylos.core.service.ZoneServiceActor]]
 */
object ZoneServiceActor {
  def props(): Props = {
    Props(new ZoneServiceActor()
      with ZoneComponent with SensorComponent with SensorTypeComponent with ZoneStatusComponent {
      val zoneDao = new ZoneDao
      val sensorDao = new SensorDao
      val sensorTypeDao = new SensorTypeDao
      val zoneStatusDao = new ZoneStatusDao
    })
  }
}

/**
 * An actor responsible for managing [[com.itylos.core.domain.Zone]] instances
 */
class ZoneServiceActor extends Actor with ActorLogging {
  this: ZoneComponent with SensorComponent with SensorTypeComponent with ZoneStatusComponent =>


  def receive = {

    // --- Create zone --- //
    case CreateZoneRq(user, zone) =>
      sensorDao.checkSensorsExistenceByOid(zone.sensorOIds)
      zoneDao.save(zone)
      val zoneId = zoneDao.getAllZones.sortWith(_.dateCreated > _.dateCreated).head.oid.get
      // Create the corresponding zone status
      val zoneStatus = ZoneStatus(zoneId, ENABLED, zone.dateCreated)
      zoneStatusDao.save(zoneStatus)
      sender() ! GetZonesRs(convert2DTOs(zoneDao.getAllZones))

    // --- Update zone --- //
    case UpdateZoneRq(user, zone) =>
      sensorDao.checkSensorsExistenceByOid(zone.sensorOIds)
      zoneDao.checkZonesExistence(List(zone.oid.get))
      zoneDao.update(zone)
      sender() ! GetZonesRs(convert2DTOs(zoneDao.getAllZones))

    // --- Delete zone --- //
    case DeleteZoneRq(user, oid) =>
      zoneStatusDao.deleteZoneStatusByZoneId(oid)
      zoneDao.deleteZoneByObjectId(oid)
      sender() ! GetZonesRs(convert2DTOs(zoneDao.getAllZones))

    // --- Remove sensor from all associated zones ---- //
    case RemoveSensorFromZone(sensorOId) =>
      val zonesWithSensor = zoneDao.getAllZones.filter(z => z.sensorOIds.contains(sensorOId))
      zonesWithSensor.foreach(z => {
        z.sensorOIds = z.sensorOIds.filter(s => s != sensorOId)
        zoneDao.update(z)
      })

    // --- Get all zones --- //
    case GetZonesRq(user) =>
      sender() ! GetZonesRs(convert2DTOs(zoneDao.getAllZones))

  }

  /**
   * Convert [[com.itylos.core.domain.Zone]] to [[com.itylos.core.rest.dto.ZoneDto]]
   * @param zones the [[com.itylos.core.domain.Zone]] objects to convert
   * @return the converted DTO objects
   */
  private def convert2DTOs(zones: List[Zone]): List[ZoneDto] = {
    // For each zone
    for (zone <- zones) yield {
      // For each sensor in the zone
      val sensors = for (sensorOId <- zone.sensorOIds) yield {
        // Setup the SensorDto
        val sensorData = sensorDao.getSensorByObjectId(sensorOId).get
        val sensorTypeData = sensorTypeDao.getSensorTypeByObjectId(sensorData.sensorTypeId).get
        new SensorDto(sensorData, sensorTypeData, None)
      }
      // Get the status for the zone
      val zoneStatus = zoneStatusDao.getZoneStatusByZoneId(zone.oid.get).get
      new ZoneDto(zone, zoneStatus, Some(sensors))
    }
  }


}