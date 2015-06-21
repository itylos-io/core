package com.itulos.controller.service.protocol

import com.itulos.controller.domain.SystemSettings
import com.itulos.controller.rest.dto.SettingsDto


/**
 * Describes the messages needed for SystemSettings management
 */
sealed trait SettingsProtocol extends Protocol

/**
 * Message to setup initial settings
 */
case class SetupInitialSettingRq() extends SettingsProtocol


/**
 * Message to update SystemSettings
 */
case class UpdateSystemSettingsRq(settings: SystemSettings) extends SettingsProtocol

/**
 * Message to get SystemSettings
 */
case class GetSystemSettingsRq() extends SettingsProtocol

/**
 * Response message to GetSystemSettingsRq
 */
case class GetSystemSettingsRs(settings:SettingsDto) extends SettingsProtocol
