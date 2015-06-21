package com.itulos.controller.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao.{SensorComponent, SensorTypeComponent, ZoneComponent, ZoneStatusComponent}
import com.itulos.controller.domain.{Zone, ZoneStatus}
import com.itulos.controller.rest.dto.{SensorDto, ZoneDto}
import com.itulos.controller.service.protocol._

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
 * An actor responsible for managing zones
 */
class ZoneServiceActor extends Actor with ActorLogging {
  this: ZoneComponent with SensorComponent with SensorTypeComponent with ZoneStatusComponent =>


  def receive = {
    // --- Create zone --- //
    case CreateZoneRq(zone) =>
      sensorDao.checkSensorsExistenceByOid(zone.sensorOIds)
      zoneDao.save(zone)
      sender() ! GetZonesRs(convert2DTOs(zoneDao.getAllZones))
    // --- Update zone --- //
    case UpdateZoneRq(zone) =>
      sensorDao.checkSensorsExistenceByOid(zone.sensorOIds)
      zoneDao.checkZonesExistence(List(zone.oid.get))
      zoneDao.update(zone)
      sender() ! GetZonesRs(convert2DTOs(zoneDao.getAllZones))
    // --- Delete zone --- //
    case DeleteZoneRq(oid) =>
      zoneStatusDao.deleteZoneStatusByZoneId(oid)
      zoneDao.deleteZoneByObjectId(oid)
      sender() ! GetZonesRs(convert2DTOs(zoneDao.getAllZones))
    // Remove sensor from all associated zones
    case RemoveSensorFromZone(sensorOId) =>
      val zonesWithSensor = zoneDao.getAllZones.filter(z => z.sensorOIds.contains(sensorOId))
      zonesWithSensor.foreach(z => {
        z.sensorOIds = z.sensorOIds.filter(s => s != sensorOId)
        zoneDao.update(z)
      })
    // --- Get all zones --- //
    case GetZonesRq() =>
      sender() ! GetZonesRs(convert2DTOs(zoneDao.getAllZones))

  }

  /**
   * Convert Zone to ZoneDto
   * @param zones the zones to convert
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
      var zoneStatus = zoneStatusDao.getZoneStatusByZoneId(zone.oid.get)
      if (zoneStatus == None) {
        zoneStatus = Some(ZoneStatus(zone.oid.get))
        zoneStatusDao.save(zoneStatus.get)
      }
      new ZoneDto(zone, zoneStatusDao.getZoneStatusByZoneId(zone.oid.get).get, Some(sensors))
    }
  }


}