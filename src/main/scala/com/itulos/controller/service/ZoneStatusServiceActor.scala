package com.itulos.controller.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao.{UserDaoComponent, ZoneComponent, ZoneStatusComponent}
import com.itulos.controller.domain.ZoneStatus
import com.itulos.controller.rest.dto.ZoneStatusDto
import com.itulos.controller.service.protocol._

object ZoneStatusServiceActor {
  def props(): Props = {
    Props(new ZoneStatusServiceActor() with ZoneStatusComponent
      with ZoneComponent with UserDaoComponent {
      val zoneStatusDao = new ZoneStatusDao
      val zoneDao = new ZoneDao
      val userDao = new UserDao
    })
  }
}

/**
 * An actor responsible for managing ZoneStatus
 */
class ZoneStatusServiceActor extends Actor with ActorLogging {
  this: ZoneStatusComponent with ZoneComponent with UserDaoComponent =>

  def receive = {
    //--- Update the status of the zone --- //
    case UpdateZoneStatus(zoneStatus) =>
      zoneDao.checkZonesExistence(List(zoneStatus.zoneId))
      zoneStatusDao.update(zoneStatus)
      sender() ! GetZoneStatusRs(convert2DTOs(zoneStatusDao.getAllZonesStatus))
    //--- Get the status of the zone --- //
    case GetCurrentZoneStatusRq() =>
      sender() ! GetZoneStatusRs(convert2DTOs(zoneStatusDao.getAllZonesStatus))
  }

  /**
   * Convert ZoneStatus to ZoneStatusDto
   * @param zonesStatus the statuses to convert
   * @return the converted DTO objects
   */
  private def convert2DTOs(zonesStatus: List[ZoneStatus]): List[ZoneStatusDto] = {
    for (status <- zonesStatus) yield {
      new ZoneStatusDto(status, zoneDao.getZoneByObjectId(status.zoneId).get)
    }
  }

}