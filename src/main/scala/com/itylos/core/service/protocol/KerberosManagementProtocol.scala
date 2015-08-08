package com.itylos.core.service.protocol

import com.itylos.core.domain.KerberosInstance
import com.itylos.core.exception.AlarmStatusEnum


/**
 * Describes the messages needed for [[com.itylos.core.service.KerberosManagementActor]]
 */
sealed trait KerberosManagementProtocol extends Protocol

/**
 * Message to change the status of kerberos instances
 */
case class UpdateKerberosInstances(alarmStatus: AlarmStatusEnum) extends KerberosManagementProtocol

/**
 * Message to configure kerberos instances' webhooks values
 */
case class ConfigureKerberosInstances(instances: List[KerberosInstance]) extends KerberosManagementProtocol

/**
 * Message that indicates motion has been detected from a kerberos instance
 */
case class MotionDetected(instance: String,pathToImage:String,ip:String) extends KerberosManagementProtocol

/**
 * Message to check if a kerberos instance has no activity in the last period
 */
case class CheckKerberosInactivity() extends  KerberosManagementProtocol

/**
 * Message to request a kerberos instance for it's configured name
 */
case class GetKerberosInstanceRq(ip: String,username:String,password:String) extends KerberosManagementProtocol

