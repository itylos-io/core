package com.itylos.core.rest.dto

import com.itylos.core.service.protocol.Protocol

/**
 * DTO for SystemOSStats
 */
case class SystemOSStatsDto(totalDisk: String,
                            freeDisk: String,
                            freeDiskPercentage: Int,
                            totalMemory: String,
                            freeMemory: String,
                            freeMemoryPercentage: Int,
                            cpuUsage: Int) extends Protocol{


}
