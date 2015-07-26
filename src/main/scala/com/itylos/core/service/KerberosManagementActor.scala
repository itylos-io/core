package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao._
import com.itylos.core.domain.{KerberosInstance, KerberosSettings}
import com.itylos.core.exception.ARMED
import com.itylos.core.service.protocol._

import scalaj.http.{Http, HttpOptions}

/**
 * Companion object to properly initiate [[com.itylos.core.service.KerberosManagementActor]]
 */
object KerberosManagementActor {
  def props(): Props = {
    Props(new KerberosManagementActor() with SettingsComponent {
      val settingsDao = new SettingsDao
    })
  }
}

/**
 * An actor responsible for enabling/disabling kerberos instances
 */
class KerberosManagementActor extends Actor with ActorLogging {
  this: SettingsComponent =>

  def receive = {

    // --- enable/disable kerberos instances --- //
    case UpdateKerberosInstances(status) =>
      val settings = settingsDao.getSettings.get.kerberosSettings
      val kerberosIsEnabled = settings.isEnabled
      if (kerberosIsEnabled) {
        val newStatus = if (status == ARMED) "enabled" else "disabled"
        settings.kerberosInstances.foreach(instance => {
          log.info("Updating status of kerberos instance [{}]", instance.instanceName)
          changeKerberosInstanceStatus(newStatus, settings, instance)
        })
      }

    // --- handle event from kerberos instance and convert to sensor event --- //

  }

  /**
   * Make http to a specific kerberos instance to enable/disable
   * @param status the new status
   */
  private def changeKerberosInstanceStatus(status: String, settings: KerberosSettings, kerberosInstance: KerberosInstance): Unit = {
    val basicAuth = kerberosInstance.username + ":" + kerberosInstance.password
    val uri = "http://" + kerberosInstance.ip + settings.kerberosConditionUpdateEnpoint + "/" + status
    Http(uri).header("Content-Type", "application/json").header("Charset", "UTF-8")
      .header("Authorization", basicAuth)
      .option(HttpOptions.readTimeout(10000)).asString
  }

}