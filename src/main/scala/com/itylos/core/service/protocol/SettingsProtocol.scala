package com.itylos.core.service.protocol

import com.itylos.core.domain._
import com.itylos.core.rest.dto.SettingsDto


/**
 * Describes the messages needed for SystemSettings management
 */
sealed trait SettingsProtocol extends Protocol

/**
 * Message to setup initial settings
 */
case class SetupInitialSettingRq() extends SettingsProtocol

/**
 * Message to update NexmoSettings
 */
case class UpdateNexmoSettingsRq(nexmoSettings: NexmoSettings) extends SettingsProtocol

/**
 * Message to update EmailSettings
 */
case class UpdateEmailSettingsRq(emailSettings: EmailSettings) extends SettingsProtocol

/**
 * Message to update SystemSettings
 */
case class UpdateSystemSettingsRq(systemSettings: SystemSettings) extends SettingsProtocol

/**
 * Message to update PushBulletSettings
 */
case class UpdatePushBulletSettingsRq(pushBulletSettings: PushBulletSettings) extends SettingsProtocol

/**
 * Message to update WebHookSettings
 */
case class UpdateWebHookSettingsRq(webHookSettings: WebHookSettings) extends SettingsProtocol

/**
 * Message to update KerberosSettings
 */
case class UpdateKerberosSettingsRq(kerberosSettings: KerberosSettings) extends SettingsProtocol

/**
 * Message to get SystemSettings
 */
case class GetSystemSettingsRq() extends SettingsProtocol

/**
 * Message to request PushBullet for the user's devices
 */
case class GetPushBulletDevicesRq(accessToken: String) extends SettingsProtocol

/**
 * Response message to GetSystemSettingsRq
 */
case class GetSystemSettingsRs(settings: SettingsDto) extends SettingsProtocol

/**
 * Response message to GetPushBulletDevicesRq
 */
case class GetPushBulletDevicesRs(devices: List[PushBulletDevice]) extends SettingsProtocol

/**
 * Response message to GetKerberosInstanceRq
 */
case class GetKerberosInstanceRs(instance: KerberosInstance) extends SettingsProtocol
