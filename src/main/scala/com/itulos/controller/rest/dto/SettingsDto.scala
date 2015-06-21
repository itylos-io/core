package com.itulos.controller.rest.dto

import com.itulos.controller.domain.{EmailSettings, NexmoSettings, SystemSettings}

/**
 * DTO for SystemSettings
 */
case class SettingsDto(maxAlarmPasswordRetries: Option[Int] = None,
                       maxSecondsToDisarm: Option[Int] = None,
                       secondsToCompletelyArm: Option[Int] = None,
                       emailSettings: Option[EmailSettingsDto] = None,
                       nexmoSettings: Option[NexmoSettingsDto] = None) {


  /**
   * Constructor with SystemSettings
   * @param systemSettings the SystemSettings to get data from
   */
  def this(systemSettings: SystemSettings) {
    this(
      Some(systemSettings.maxAlarmPasswordRetries),
      Some(systemSettings.maxSecondsToDisarm),
      Some(systemSettings.secondsToCompletelyArm),
      Some(new EmailSettingsDto(systemSettings.emailSettings)),
      Some(new NexmoSettingsDto(systemSettings.nexmoSettings))
    )
  }

}

case class NexmoSettingsDto(isEnabled: Option[Boolean] = None,
                            nexmoKey: Option[String] = None,
                            nexmoSecret: Option[String] = None,
                            mobilesToNotify: Option[List[String]] = None) {

  /**
   * Constructor with a EmailSettings
   */
  def this(nexmoSettings: NexmoSettings) {
    this(
      Some(nexmoSettings.isEnabled),
      Some(nexmoSettings.nexmoKey),
      Some(nexmoSettings.nexmoSecret),
      Some(nexmoSettings.mobilesToNotify)
    )
  }

}


case class EmailSettingsDto(isEnabled: Option[Boolean] = None,
                            smtpStartTLSEnabled: Option[Boolean] = None,
                            smtpHost: Option[String] = None,
                            smtpUser: Option[String] = None,
                            smtpPassword: Option[String] = None,
                            smtpPort: Option[Int] = None,
                            smtpAuth: Option[Boolean] = None,
                            emailsToNotify: Option[List[String]] = None) {

  /**
   * Constructor with a EmailSettings
   */
  def this(emailSettings: EmailSettings) {
    this(
      Some(emailSettings.isEnabled),
      Some(emailSettings.smtpStartTLSEnabled),
      Some(emailSettings.smtpHost),
      Some(emailSettings.smtpUser),
      Some(emailSettings.smtpPassword),
      Some(emailSettings.smtpPort),
      Some(emailSettings.smtpAuth),
      Some(emailSettings.emailsToNotify)
    )
  }

}
