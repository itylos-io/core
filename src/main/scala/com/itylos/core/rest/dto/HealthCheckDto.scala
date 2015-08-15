package com.itylos.core.rest.dto

import com.itylos.core.domain.HealthCheck

/**
 * DTO for [[com.itylos.core.domain.HealthCheck]]
 */
case class HealthCheckDto(oid: String,
                          url: String,
                          lastCheckStatusCode: Int,
                          lastTimeChecked: Long,
                          checkInterval: Int) {

  /**
   * Constructor with a [[com.itylos.core.domain.HealthCheck]]
   */
  def this(healthCheck: HealthCheck) {
    this(
      healthCheck.oid.get,
      healthCheck.url,
      healthCheck.lastCheckStatusCode,
      healthCheck.lastTimeChecked,
      healthCheck.checkInterval
    )
  }

}
