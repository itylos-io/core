package com.itylos.core.rest.dto

import com.itylos.core.domain.NexmoSettings


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
