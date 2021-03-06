package com.itylos.core.service

import java.io.StringWriter
import java.util

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao._
import com.itylos.core.domain.{AlarmStatus, HealthCheck}
import com.itylos.core.service.protocol.AlarmTriggeredNotification
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.joda.time.{DateTime, DateTimeZone}

object EmailServiceActor {
  def props(): Props = {
    Props(new EmailServiceActor() with ZoneComponent with AlarmStatusComponent with SensorComponent
      with SettingsComponent with HealthCheckComponent {
      val zoneDao = new ZoneDao
      val sensorDao = new SensorDao
      val alarmStatusDao = new AlarmStatusDao
      val settingsDao = new SettingsDao
      val healthCheckDao = new HealthCheckDao
    })
  }
}

/**
 * An actor responsible for managing alarms
 */
class EmailServiceActor extends Actor with ActorLogging {
  this: ZoneComponent with AlarmStatusComponent with SensorComponent with SettingsComponent with HealthCheckComponent =>


  override def preStart() {
    log.info("Starting email service...")
  }


  def receive = {
    // --- Send email for alarm violation --- //
    case AlarmTriggeredNotification() =>
      val alarmStatus = alarmStatusDao.getAlarmStatus.get
      if (!alarmStatus.emailNotificationsSent && settingsDao.getSettings.get.emailSettings.isEnabled) {
        alarmStatus.emailNotificationsSent = true
        alarmStatusDao.update(alarmStatus)
        val htmlData = if (!alarmStatus.healthCheckFailed) {
          log.info("Sending emails for zone [{}]", alarmStatus.zoneIds)
          getHtmlData(alarmStatus)
        } else {
          log.info("Sending emails due to health check failure")
          val failedChecks = healthCheckDao.getAllHealthChecks.filter(hc => hc.lastCheckStatusCode != 200)
          getFailedToHealthCheckEmailMessage(failedChecks)
        }
        sendEmailAlert(alarmStatus, htmlData)
      }
  }


  def sendEmailAlert(alarmStatus: AlarmStatus, htmlData: String): Unit = {
    import courier.Defaults._
    // DO NOT REMOVE ME
    import courier._
    val emailSettings = settingsDao.getSettings.get.emailSettings
    if (!emailSettings.isEnabled) return

    val mailer = Mailer(emailSettings.smtpHost, emailSettings.smtpPort)
      .auth(emailSettings.smtpAuth)
      .as(emailSettings.smtpUser, emailSettings.smtpPassword)
      .startTtls(emailSettings.smtpStartTLSEnabled)()

    emailSettings.emailsToNotify.foreach(email => {
      mailer(Envelope.from("admin" `@` "itylos.com")
        .to(email.split("@")(0) `@` email.split("@")(1))
        .subject("Itylos Alert @ " + new DateTime()
        .withZone(DateTimeZone.UTC).toString)
        .content(Multipart().html(htmlData))).onSuccess {
        case _ => log.info("Emails delivered to " + email)
      }
    })
  }

  /**
   * Setup the html string for the email
   * @param alarmStatus the AlarmStatus to retrieve data from
   * @return the html string
   */
  def getHtmlData(alarmStatus: AlarmStatus): String = {
    val ve = new VelocityEngine()
    ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
    ve.setProperty("classpath.resource.loader.class", classOf[ClasspathResourceLoader].getName)
    ve.init()
    val context = new VelocityContext()
    val t = ve.getTemplate("templates/alarm_bypassed_email.vm")

    // Zones
    val zones = new util.ArrayList[util.HashMap[String, String]]()
    alarmStatus.zoneIds.foreach(zone => {
      val map = new util.HashMap[String, String]()
      map.put("name", zoneDao.getZoneByObjectId(zone).get.name)
      zones.add(map)
    })

    // Sensors
    val sensors = new util.ArrayList[util.HashMap[String, String]]()
    alarmStatus.sensorOIds.foreach(zone => {
      val map = new util.HashMap[String, String]()
      map.put("name", sensorDao.getSensorByObjectId(zone).get.name)
      sensors.add(map)
    })

    context.put("zones", zones)
    context.put("sensors", sensors)
    context.put("time", new DateTime().withMillis(alarmStatus.violationTime).withZone(DateTimeZone.UTC).toString)
    val sw = new StringWriter
    t.merge(context, sw)
    sw.toString
  }


  /**
   * Setup the html string for the email associated to health check failure
   */
  def getFailedToHealthCheckEmailMessage(failedHealthChecks: List[HealthCheck]): String = {

    val healthChecks = new util.ArrayList[util.HashMap[String, String]]()
    failedHealthChecks.foreach(failedHealthCheck => {
      val map = new util.HashMap[String, String]()
      map.put("url", failedHealthCheck.url)
      healthChecks.add(map)
    })

    val ve = new VelocityEngine()
    ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
    ve.setProperty("classpath.resource.loader.class", classOf[ClasspathResourceLoader].getName)
    ve.init()
    val context = new VelocityContext()
    val t = ve.getTemplate("templates/health_check_failed_email.vm")
    context.put("healthChecks", healthChecks)
    context.put("time", new DateTime().withZone(DateTimeZone.UTC).toString)
    val sw = new StringWriter
    t.merge(context, sw)
    sw.toString
  }

}