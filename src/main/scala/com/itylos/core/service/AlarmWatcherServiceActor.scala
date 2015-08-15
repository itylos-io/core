package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao._
import com.itylos.core.domain._
import com.itylos.core.exception.ARMED
import com.itylos.core.service.protocol._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

object AlarmWatcherServiceActor {
  def props(): Props = {
    Props(new AlarmWatcherServiceActor() with ZoneComponent
      with ZoneStatusComponent with AlarmStatusComponent with SensorComponent with SettingsComponent with HealthCheckComponent {
      val zoneDao = new ZoneDao
      val sensorDao = new SensorDao
      val zoneStatusDao = new ZoneStatusDao
      val alarmStatusDao = new AlarmStatusDao
      val settingsDao = new SettingsDao
      val healthCheckDao = new HealthCheckDao
    })
  }
}

/**
 * An actor responsible for managing alarms
 */
class AlarmWatcherServiceActor extends Actor with ActorLogging {
  this: ZoneStatusComponent with ZoneComponent
    with AlarmStatusComponent with SensorComponent with SettingsComponent with HealthCheckComponent =>

  var ENABLED_ALARM_TIMEOUT = 15 * 1000
  var ENABLED_ALARM_MAX_RETRIES = 3
  var SECONDS_COMPLETELY_ARM = 15

  override def preStart() {
    import scala.concurrent.duration._
    context.system.scheduler.schedule(1.seconds, 3.seconds, self, CheckEnabledAlarms())
    reloadSettings()
    log.info("Starting alarm watcher service...")
  }


  def receive = {

    // --- New sensor event --- //
    case NewSensorEvent(sensorEvent) =>
      reloadSettings()
      val alarmStatus = alarmStatusDao.getAlarmStatus.get
      // Check if the armBypass time has been elapsed and if so  add the violated zone to the alarm
      // & the violated sensor to the alarm status
      if (alarmStatus.status == ARMED && alarmStatus.timeArmed + SECONDS_COMPLETELY_ARM < new DateTime().getMillis) {
        log.info("About to trigger alarm if system is not disarmed before timeout...")
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
      reloadSettings()
      var shouldTriggerAlarm = false
      val nowTime = new DateTime().getMillis
      val alarmStatus = alarmStatusDao.getAlarmStatus.get
      // Check if the time to disarm alarm been timeout OR max retries have been exhausted
      if (alarmStatus.status == ARMED && alarmStatus.violationTime != -1
        && ((alarmStatus.violationTime + ENABLED_ALARM_TIMEOUT < nowTime)
        || (alarmStatus.falseEnteredPasswords >= ENABLED_ALARM_MAX_RETRIES))) {
        shouldTriggerAlarm = true
      }

      // Check if a health check has failed
      if (alarmStatus.status == ARMED) {
        healthCheckDao.getAllHealthChecks.foreach(healthCheck => {
          if (healthCheck.lastCheckStatusCode != 200) {
            shouldTriggerAlarm = true
            alarmStatus.healthCheckFailed = true
            alarmStatusDao.update(alarmStatus)
          }
        })
      }

      if (shouldTriggerAlarm) {
        // TODO Use notifier
        log.warning("Alarm triggered!!!")
        context.actorSelection("/user/emailService") ! AlarmTriggeredNotification()
        context.actorSelection("/user/smsService") ! AlarmTriggeredNotification()
        context.actorSelection("/user/pushBulletServiceActor") ! AlarmTriggeredNotification()
        context.actorSelection("/user/soundServiceActor") ! AlarmTriggeredNotification()
        context.actorSelection("/user/webHooksServiceActor") ! AlarmTriggeredNotification()
      }
  }

  /**
   * Reload settings
   */
  def reloadSettings(): Unit = {
    SECONDS_COMPLETELY_ARM = settingsDao.getSettings.get.systemSettings.delayToArm * 1000
    ENABLED_ALARM_TIMEOUT = settingsDao.getSettings.get.systemSettings.maxSecondsToDisarm * 1000
    ENABLED_ALARM_MAX_RETRIES = settingsDao.getSettings.get.systemSettings.maxAlarmPasswordRetries
  }

  /**
   * Get the zones this sensors belongs to
   * @param sensorId the id of the zone
   * @return the corresponding [[com.itylos.core.domain.ZoneStatus]] instances
   */
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