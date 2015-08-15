package com.itylos.core.service.protocol

import com.itylos.core.rest.dto.HealthCheckDto


/**
  * Describes the messages needed for health checks management
  */
sealed trait HealthCheckProtocol extends Protocol

/**
 * Message to update health check urls
 */
case class UpdateHealthCheckUrls(urls:List[String]) extends HealthCheckProtocol

/**
 * Message to make health check requests to configured url
 */
case class HealthCheckUrls() extends  HealthCheckProtocol


/**
 * Message to request for configured health checks
 */
case class GetHealthChecksRq() extends  HealthCheckProtocol


/**
 * Message that holds data for all configured health checks
 */
case class GetHealthChecksRs(healthChecks:List[HealthCheckDto]) extends  HealthCheckProtocol
