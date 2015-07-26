package com.itylos.core.service.protocol

import com.itylos.core.exception.AlarmStatusEnum


/**
 * Describes the messages needed for [[com.itylos.core.service.KerberosManagementActor]]
 */
sealed trait KerberosManagementProtocol extends Protocol

/**
 * Message to change the status of kerberos instances
 */
case class UpdateKerberosInstances(alarmStatus: AlarmStatusEnum) extends KerberosManagementProtocol
