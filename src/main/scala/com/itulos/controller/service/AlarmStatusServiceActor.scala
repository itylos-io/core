package com.itulos.controller.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao._
import com.itulos.controller.domain._
import com.itulos.controller.exception.{ARMED, CannotArmWithNoEnabledZone}
import com.itulos.controller.rest.dto._
import com.itulos.controller.service.protocol._
import org.joda.time.DateTime

object AlarmStatusServiceActor {
  def props(): Props = {
    Props(new AlarmStatusServiceActor() with AlarmStatusComponent
      with WebSocketNotifier with AlarmStatusHistoryComponent with UserDaoComponent with ZoneStatusComponent {
      val alarmStatusDao = new AlarmStatusDao
      val alarmStatusHistoryDao = new AlarmStatusHistoryDao
      val userDao = new UserDao()
      val zoneStatusDao = new ZoneStatusDao
    })
  }
}

/**
 * An actor responsible for managing alarms
 */
class AlarmStatusServiceActor extends Actor with ActorLogging with WebSocketNotifier {
  this: AlarmStatusComponent with AlarmStatusHistoryComponent with UserDaoComponent with ZoneStatusComponent =>

  def receive = {

    // --- Setup alarm status when no entry does not exist --- //
    case SetupAlarmStatus() =>
      val alarmStatus = alarmStatusDao.getAlarmStatus
      if (alarmStatus == None) {
        log.info("Did not found any alarm status in database. Creating a new one...")
        alarmStatusDao.save(new AlarmStatus())
      }

    // --- Update status of the alarm --- //
    case UpdateAlarmStatus(status, user) =>
      // Assert there is at least one enabled zone
      val enabledZones = zoneStatusDao.getAllZonesStatus.filter(z => z.status == ENABLED)
      if (enabledZones.isEmpty) {
        throw new CannotArmWithNoEnabledZone()
      }
      // Change state
      val alarmStatus = alarmStatusDao.getAlarmStatus.get
      if (status == ARMED) {
        alarmStatus.timeArmed = new DateTime().getMillis
        alarmStatus.userIdArmed = user.oid.get
      } else {
        alarmStatus.resetAlarmStatus()
        alarmStatus.timeDisArmed = new DateTime().getMillis
        alarmStatus.userIdDisarmed = user.oid.get
      }
      alarmStatus.status = status
      alarmStatusDao.update(alarmStatus)
      updateHistory(alarmStatus)
      notifyWebSocket(context, Event(AlarmStatusRs(new AlarmStatusDto(alarmStatusDao.getAlarmStatus.get, user))))
      sender() ! AlarmStatusRs(new AlarmStatusDto(alarmStatusDao.getAlarmStatus.get, user))

    // --- Get alarm status history --- //
    case GetAlarmStatusHistoryRq(limit, offset) =>
      val data = alarmStatusHistoryDao.getAlarmStatuses(limit, offset)
      sender() ! AlarmStatusHistoryRs(convert2DTOs(data))

    // --- Get current alarm status --- //
    case GetCurrentAlarmStatusRq() =>
      sender() ! AlarmStatusRs(AlarmStatusDto(alarmStatusDao.getAlarmStatus.get.status.toString))

  }

  /**
   * Convert alarmStatusHistory to alarmStatusHistoryDto
   * @param statuses the statuses to convert
   * @return the DTOs
   */
  private def convert2DTOs(statuses: List[AlarmStatusHistory]): List[AlarmStatusHistoryDto] = {
    for (status <- statuses) yield {
      val userIdToFetch = if (status.status == ARMED) status.userIdArmed else status.userIdDisarmed
      new AlarmStatusHistoryDto(status, userDao.getUserByObjectId(userIdToFetch).get)
    }
  }

  /**
   * Add entry to history collection
   * @param alarmStatus the AlarmStatus to add to history
   */
  private def updateHistory(alarmStatus: AlarmStatus): Unit = {
    val alarmStatusHistory = new AlarmStatusHistory(alarmStatus)
    alarmStatusHistoryDao.save(alarmStatusHistory)
  }
}