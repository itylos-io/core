package com.itylos.core.rest.dto

import com.itylos.core.domain.{KerberosInstance, KerberosSettings}


case class KerberosSettingsDto(isEnabled: Boolean,
                               instances: List[KerberosInstance]) {

  /**
   * Constructor with a KerberosSettings
   */
  def this(kerberosSettings: KerberosSettings) {
    this(
      kerberosSettings.isEnabled,
      kerberosSettings.kerberosInstances
    )
  }

}
