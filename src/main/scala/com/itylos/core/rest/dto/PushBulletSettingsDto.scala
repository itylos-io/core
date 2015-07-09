package com.itylos.core.rest.dto

import com.itylos.core.domain.{PushBulletDevice, PushBulletSettings}


case class PushBulletSettingsDto(isEnabled: Boolean,
                                 accessToken: String,
                                 notifyForSensorEvents: Boolean,
                                 notifyForAlarms: Boolean,
                                 notifyForAlarmsStatusUpdates: Boolean,
                                 devices: List[PushBulletDevice]) {

  /**
   * Constructor with a PushBulletSettings
   */
  def this(pushBulletSettings: PushBulletSettings) {
    this(
      pushBulletSettings.isEnabled,
      pushBulletSettings.accessToken,
      pushBulletSettings.notifyForSensorEvents,
      pushBulletSettings.notifyForAlarms,
      pushBulletSettings.notifyForAlarmsStatusUpdates,
      pushBulletSettings.devices
    )
  }

}
