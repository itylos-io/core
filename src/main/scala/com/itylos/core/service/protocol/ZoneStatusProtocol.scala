package com.itylos.core.service.protocol

import com.itylos.core.domain.{User, ZoneStatus}
import com.itylos.core.rest.dto.ZoneStatusDto


/**
 * Describes the messages needed for ZoneStatus data management
 */
sealed trait ZoneStatusProtocol extends Protocol

/**
 * Message to update status of the zone
 */
case class UpdateZoneStatus(user:User,zoneStatus:ZoneStatus) extends ZoneStatusProtocol

/**
 * Message to get status of the alarm zones
 */
case class GetCurrentZoneStatusRq(user:User) extends ZoneStatusProtocol

/**
 * Response message to GetAlarmStatusRq
 */
case class GetZoneStatusRs(zonesStatus:List[ZoneStatusDto]) extends ZoneStatusProtocol
