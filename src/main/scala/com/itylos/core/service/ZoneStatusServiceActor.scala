package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao.{ZoneComponent, ZoneStatusComponent}
import com.itylos.core.domain.ZoneStatus
import com.itylos.core.rest.dto.ZoneStatusDto
import com.itylos.core.service.protocol._

/**
 * Companion object to properly start [[com.itylos.core.service.ZoneStatusServiceActor]]
 */
object ZoneStatusServiceActor {
  def props(): Props = {
    Props(new ZoneStatusServiceActor() with ZoneStatusComponent
      with ZoneComponent {
      val zoneStatusDao = new ZoneStatusDao
      val zoneDao = new ZoneDao
    })
  }
}

/**
 * Companion object to properly initiate [[com.itylos.core.service.ZoneStatusServiceActor]]
 */
class ZoneStatusServiceActor extends Actor with ActorLogging {
  this: ZoneStatusComponent with ZoneComponent =>

  def receive = {

    //--- Update the status of a zone --- //
    case UpdateZoneStatus(user, zoneStatus) =>
      zoneDao.checkZonesExistence(List(zoneStatus.zoneId))
      zoneStatusDao.update(zoneStatus)
      sender() ! GetZoneStatusRs(convert2DTOs(zoneStatusDao.getAllZonesStatus))

    //--- Get the status for each zone --- //
    case GetCurrentZoneStatusRq(user) =>
      sender() ! GetZoneStatusRs(convert2DTOs(zoneStatusDao.getAllZonesStatus))
  }

  /**
   * Convert [[com.itylos.core.domain.ZoneStatus]] to [[com.itylos.core.rest.dto.ZoneStatusDto]]
   * @param zonesStatus the list of  [[com.itylos.core.domain.ZoneStatus]] objects to convert
   * @return the converted DTO objects
   */
  private def convert2DTOs(zonesStatus: List[ZoneStatus]): List[ZoneStatusDto] = {
    for (status <- zonesStatus) yield {
      new ZoneStatusDto(status, zoneDao.getZoneByObjectId(status.zoneId).get)
    }
  }

}