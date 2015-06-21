package com.itulos.controller.rest.dto

import com.itulos.controller.service.protocol.Protocol

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
