package com.itylos.core.rest.dto

import com.itylos.core.domain.EmailSettings


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
