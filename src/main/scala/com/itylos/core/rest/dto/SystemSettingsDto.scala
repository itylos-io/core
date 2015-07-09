package com.itylos.core.rest.dto

import com.itylos.core.domain.SystemSettings


case class SystemSettingsDto(maxAlarmPasswordRetries: Int,
                             maxSecondsToDisarm: Int,
                             delayToArm: Int,
                             playSoundsForSensorEvents: Boolean,
                             playSoundsForTriggeredAlarm: Boolean,
                             playSoundsForAlarmStatusUpdates: Boolean,
                             accessToken: String) {


  /**
   * Constructor with a SystemSettings
   */
  def this(systemSettings: SystemSettings) {
    this(
      systemSettings.maxAlarmPasswordRetries,
      systemSettings.maxSecondsToDisarm,
      systemSettings.delayToArm,
      systemSettings.playSoundsForSensorEvents,
      systemSettings.playSoundsForTriggeredAlarm,
      systemSettings.playSoundsForAlarmStatusUpdates,
      systemSettings.accessToken
    )
  }

}
