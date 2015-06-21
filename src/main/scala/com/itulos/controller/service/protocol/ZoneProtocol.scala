package com.itulos.controller.service.protocol

import com.itulos.controller.domain.Zone
import com.itulos.controller.rest.dto.ZoneDto


/**
 * Describes the messages needed for zones management
 */
sealed trait ZoneProtocol extends Protocol

/**
 * Message to add new zone
 */
case class CreateZoneRq(zone: Zone) extends ZoneProtocol

/**
 * Message to update new zone
 */
case class UpdateZoneRq(zone: Zone) extends ZoneProtocol

/**
 * Message to delete a zone
 * @param oid zone Id to delete
 */
case class DeleteZoneRq(oid:String) extends ZoneProtocol

/**
 * Message to get all zones
 */
case class GetZonesRq() extends ZoneProtocol

/**
 * Response message to GetZonesRq
 */
case class GetZonesRs(zones:List[ZoneDto]) extends ZoneProtocol

/**
 * Message to remove a sensor from a zone
 * @param sensorOId the oid of the sensor to remove
 */
case class RemoveSensorFromZone(sensorOId:String) extends  ZoneProtocol


