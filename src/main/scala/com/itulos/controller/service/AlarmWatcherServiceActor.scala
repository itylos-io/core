package com.itulos.controller.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao._
import com.itulos.controller.domain._
import com.itulos.controller.exception.ARMED
import com.itulos.controller.service.protocol._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

object AlarmWatcherServiceActor {
  def props(): Props = {
    Props(new AlarmWatcherServiceActor() with ZoneComponent
      with ZoneStatusComponent with AlarmStatusComponent with SensorComponent with SettingsComponent {
      val zoneDao = new ZoneDao
      val sensorDao = new SensorDao
      val zoneStatusDao = new ZoneStatusDao
      val alarmStatusDao = new AlarmStatusDao
      val settingsDao = new SettingsDao
    })
  }
}

/**
 * An actor responsible for managing alarms
 */
class AlarmWatcherServiceActor extends Actor with ActorLogging {
  this: ZoneStatusComponent with ZoneComponent
    with AlarmStatusComponent with SensorComponent with SettingsComponent =>

  var ENABLED_ALARM_TIMEOUT = 15 * 1000
  var ENABLED_ALARM_MAX_RETRIES = 3
  var SECONDS_COMPLETELY_ARM = 15

  override def preStart() {
    import scala.concurrent.duration._
    context.system.scheduler.schedule(1.seconds, 3.seconds, self, CheckEnabledAlarms())

    SECONDS_COMPLETELY_ARM = settingsDao.getSettings.get.secondsToCompletelyArm*1000
    ENABLED_ALARM_TIMEOUT = settingsDao.getSettings.get.maxSecondsToDisarm * 1000
    ENABLED_ALARM_MAX_RETRIES = settingsDao.getSettings.get.maxAlarmPasswordRetries
    log.info("Starting alarm watcher...  " + ENABLED_ALARM_MAX_RETRIES)
  }

  def receive = {
    // --- New sensor event --- //
    case NewSensorEvent(sensorEvent) =>
      val alarmStatus = alarmStatusDao.getAlarmStatus.get
      // Check if the armBypass time has been elapsed and if so  add the violated zone to the alarm
      // & the violated sensor to the alarm status
      if (alarmStatus.status == ARMED && alarmStatus.timeArmed + SECONDS_COMPLETELY_ARM < new DateTime().getMillis ) {
        log.warning("Triggering alarm.")
        val zonesStatus = getEnabledZonesForSensor(sensorEvent.sensorId)
        if (zonesStatus.nonEmpty) {
          zonesStatus.foreach(zs => alarmStatus.addNewZone(zs.zoneId))
          alarmStatus.addNewSensor(sensorDao.getSensorBySensorId(sensorEvent.sensorId).get.oid.get)
          alarmStatus.violationTime = new DateTime().getMillis
          alarmStatusDao.update(alarmStatus)
        }
      }
    // --- Check if an enabled alarm should trigger external events --- //
    case CheckEnabledAlarms() =>
      val nowTime = new DateTime().getMillis
      val alarmStatus = alarmStatusDao.getAlarmStatus.get
      // Check if the time to disarm alarm been timeout OR max retries have been exhausted
      if (alarmStatus.status == ARMED && alarmStatus.violationTime != -1
        && ((alarmStatus.violationTime + ENABLED_ALARM_TIMEOUT < nowTime)
        || (alarmStatus.falseEnteredPasswords >= ENABLED_ALARM_MAX_RETRIES))) {
        context.actorSelection("/user/emailService") ! NotifyForAlarmViolation()
        context.actorSelection("/user/smsService") ! NotifyForAlarmViolation()
      }

  }


  def getEnabledZonesForSensor(sensorId: String): List[ZoneStatus] = {
    sensorDao.checkSensorsExistenceBySensorId(List(sensorId))
    val sensor = sensorDao.getSensorBySensorId(sensorId).get
    if (sensor.isActive) {
      val zones = zoneDao.getAllZones.filter(z => z.sensorOIds.contains(sensor.oid.get))
      (for (zone <- zones) yield {
        zoneStatusDao.getZoneStatusByZoneId(zone.oid.get)
      }).filter(zs => zs != None && zs.get.status == ENABLED).map(zs => zs.get)
    } else {
      List()
    }
  }


}