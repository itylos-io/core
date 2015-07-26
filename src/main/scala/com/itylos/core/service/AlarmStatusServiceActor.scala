package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao._
import com.itylos.core.domain._
import com.itylos.core.exception.{ARMED, CannotArmWithNoEnabledZone, FalsePasswordException}
import com.itylos.core.rest.dto._
import com.itylos.core.service.protocol._
import org.joda.time.DateTime

/**
 * Companion object to properly initiate [[com.itylos.core.service.AlarmStatusServiceActor]]
 */
object AlarmStatusServiceActor {
  def props(): Props = {
    Props(new AlarmStatusServiceActor() with AlarmStatusComponent
      with NotificationsHelper with AlarmStatusHistoryComponent with UserDaoComponent with ZoneStatusComponent {
      val alarmStatusDao = new AlarmStatusDao
      val alarmStatusHistoryDao = new AlarmStatusHistoryDao
      val userDao = new UserDao()
      val zoneStatusDao = new ZoneStatusDao
    })
  }
}

/**
 * An actor responsible for managing [[com.itylos.core.domain.AlarmStatus]]
 */
class AlarmStatusServiceActor extends Actor with ActorLogging with NotificationsHelper {
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
    case UpdateAlarmStatus(status,password, user) =>

      // Change state
      val alarmStatus = alarmStatusDao.getAlarmStatus.get
      if (status == ARMED) {
        // Assert there is at least one enabled zone
        if (zoneStatusDao.getAllZonesStatus.filter(z => z.status == ENABLED).isEmpty) {
          throw new CannotArmWithNoEnabledZone()
        }
        alarmStatus.timeArmed = new DateTime().getMillis
        alarmStatus.userIdArmed = user.oid.get
      } else {
        if (user.alarmPassword != password) {
          alarmStatus.falseEnteredPasswords = alarmStatus.falseEnteredPasswords+1
          alarmStatusDao.update(alarmStatus)
          throw new FalsePasswordException()
        }
        alarmStatus.resetAlarmStatus()
        alarmStatus.timeDisArmed = new DateTime().getMillis
        alarmStatus.userIdDisarmed = user.oid.get
      }
      alarmStatus.status = status
      alarmStatusDao.update(alarmStatus)
      updateHistory(alarmStatus)
      notifyAll(context, UpdatedAlarmStatusNotification(new AlarmStatusDto(alarmStatusDao.getAlarmStatus.get, user)))
      sender() ! AlarmStatusRs(new AlarmStatusDto(alarmStatus, user))

    // --- Get alarm status history --- //
    case GetAlarmStatusHistoryRq(limit, offset) =>
      val data = alarmStatusHistoryDao.getAlarmStatuses(limit, offset)
      sender() ! AlarmStatusHistoryRs(convert2DTOs(data))

    // --- Get current alarm status --- //
    case GetCurrentAlarmStatusRq() =>
      sender() ! AlarmStatusRs(AlarmStatusDto(alarmStatusDao.getAlarmStatus.get.status.toString))

  }

  /**
   * Convert [[com.itylos.core.domain.AlarmStatusHistory]] to [[com.itylos.core.rest.dto.AlarmStatusHistoryDto]]
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
   * Add [[com.itylos.core.domain.AlarmStatus]] to history collection
   * @param alarmStatus the [[com.itylos.core.domain.AlarmStatus]] to add to history
   */
  private def updateHistory(alarmStatus: AlarmStatus): Unit = {
    val alarmStatusHistory = new AlarmStatusHistory(alarmStatus)
    alarmStatusHistoryDao.save(alarmStatusHistory)
  }
}