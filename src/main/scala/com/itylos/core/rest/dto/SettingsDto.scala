package com.itylos.core.rest.dto

import com.itylos.core.domain.Settings

/**
 * DTO for Settings
 */
case class SettingsDto(systemSettings: SystemSettingsDto,
                       emailSettings: EmailSettingsDto,
                       nexmoSettings: NexmoSettingsDto,
                       webHookSettings: WebHookSettingsDto,
                       pushBulletSettings: PushBulletSettingsDto) {


  /**
   * Constructor with [[com.itylos.core.domain.Settings]]
   * @param settings the [[com.itylos.core.domain.Settings]] to get data from
   */
  def this(settings: Settings) {
    this(
      new SystemSettingsDto(settings.systemSettings),
      new EmailSettingsDto(settings.emailSettings),
      new NexmoSettingsDto(settings.nexmoSettings),
      new WebHookSettingsDto(settings.webHookSettings),
      new PushBulletSettingsDto(settings.pushBulletSettings)
    )
  }

}








