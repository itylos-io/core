package com.itylos.core.rest.dto

import com.itylos.core.domain.WebHookSettings

case class WebHookSettingsDto(isEnabled: Boolean,
                              uris: List[String]) {


  /**
   * Constructor with a WebHookSettings
   */
  def this(webHookSettings: WebHookSettings) {
    this(
      webHookSettings.isEnabled,
      webHookSettings.uris
    )
  }


}
