package com.itylos.core.service.protocol

import com.itylos.core.rest.dto.SensorEventsStatisticsDto


/**
 * Describes the messages needed for querying sensor event statistics
 */
sealed trait SensorEventStatisticsProtocol extends Protocol

/**
 * Message to get sensor events statistics
 */
case class GetStats() extends SensorEventStatisticsProtocol

/**
 * Message to remove statistics after TTL
 */
case class RemoveExpiredStats() extends SensorEventStatisticsProtocol

/**
 * Message that holds sensor event statistics
 */
case class GetStatisticsRs(lastHourStats: List[SensorEventsStatisticsDto],
                           lastDayStats: List[SensorEventsStatisticsDto],
                           lastWeekStats: List[SensorEventsStatisticsDto]) extends SensorEventStatisticsProtocol




