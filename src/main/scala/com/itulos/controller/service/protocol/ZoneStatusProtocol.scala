package com.itulos.controller.service.protocol

import com.itulos.controller.domain.ZoneStatus
import com.itulos.controller.rest.dto.ZoneStatusDto


/**
 * Describes the messages needed for ZoneStatus data management
 */
sealed trait ZoneStatusProtocol extends Protocol

/**
 * Message to update status of the zone
 */
case class UpdateZoneStatus(zoneStatus:ZoneStatus) extends ZoneStatusProtocol

/**
 * Message to get status of the alarm zones
 */
case class GetCurrentZoneStatusRq() extends ZoneStatusProtocol

/**
 * Response message to GetAlarmStatusRq
 */
case class GetZoneStatusRs(zonesStatus:List[ZoneStatusDto]) extends ZoneStatusProtocol
