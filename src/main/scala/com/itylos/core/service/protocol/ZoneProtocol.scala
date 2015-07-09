package com.itylos.core.service.protocol

import com.itylos.core.domain.{User, Zone}
import com.itylos.core.rest.dto.ZoneDto


/**
 * Describes the messages needed for zones management
 */
sealed trait ZoneProtocol extends Protocol

/**
 * Message to add new zone
 */
case class CreateZoneRq(user:User,zone: Zone) extends ZoneProtocol

/**
 * Message to update new zone
 */
case class UpdateZoneRq(user:User,zone: Zone) extends ZoneProtocol

/**
 * Message to delete a zone
 * @param oid zone Id to delete
 */
case class DeleteZoneRq(user:User,oid:String) extends ZoneProtocol

/**
 * Response message to GetZonesRq
 */
case class GetZonesRs(zones:List[ZoneDto]) extends ZoneProtocol

/**
 * Message to remove a sensor from a zone
 * @param sensorOId the oid of the sensor to remove
 */
case class RemoveSensorFromZone(sensorOId:String) extends  ZoneProtocol

/**
 * Message to get all zones
 */
case class GetZonesRq(user:User) extends ZoneProtocol
