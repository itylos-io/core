package com.itylos.core.service

import java.io.{BufferedReader, File, InputStreamReader}

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.rest.dto.{AlarmStatusDto, SystemOSStatsDto}
import com.itylos.core.service.protocol.{AlarmStatusRs, Event}
import com.itylos.core.websocket.ItylosNotificationEvents.SystemStatsMessage
import com.twitter.util.StorageUnit

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Companion object to properly initiate [[com.itylos.core.service.SystemStatsServiceActor]]
 */
object SystemStatsServiceActor {
  def props(): Props = {
    Props(new SystemStatsServiceActor() with NotificationsHelper {
    })
  }
}

case class SendStats()

/**
 * An actor responsible for sending system statistics through web sockets
 */
class SystemStatsServiceActor extends Actor with ActorLogging with NotificationsHelper {

  override def preStart() {
    log.info("Starting systems stats service...")
    import scala.concurrent.duration._
    context.system.scheduler.schedule(1.seconds, 2.seconds, self, SendStats())
  }

  def receive = {

    // --- Setup alarm status when no entry does not exist --- //
    case SendStats() =>
      val memStats = getMemoryUsage
      val diskStats = getDiskUsage
      val cputStats = getCpuUsage.toDouble.toInt
      val stats = SystemOSStatsDto(diskStats._1,diskStats._2,diskStats._3,memStats._1,memStats._2,memStats._3,cputStats)
      notifyViaWebSocket(context, Event(stats))
  }


  /**
   * @return (TotalDiskCapacity, FreeDiskSPace, FreeDiskSPacePercentage)
   */
  def getDiskUsage: (String, String, Int) = {
    val roots = File.listRoots()
    roots(0).getTotalSpace
    val diskCapacity = new StorageUnit(roots(0).getTotalSpace).toHuman()
    val freeDisk = new StorageUnit(roots(0).getFreeSpace).toHuman()
    (diskCapacity, freeDisk, ((roots(0).getFreeSpace * 100) / roots(0).getTotalSpace).toInt)
  }


  /**
   * @return (TotalMemory, FreeMemory, FreeMemoryPercentage)
   */
  def getMemoryUsage: (String, String, Int) = {
    val cmd = Array("/bin/sh", "-c", "cat /proc/meminfo")
    val process = Runtime.getRuntime.exec(cmd)
    val reader = new BufferedReader(new InputStreamReader(process.getInputStream))
    val totalMemBytes = reader.readLine().trim().split(":")(1).replace("kB", "").trim.toLong * 1000
    val freeMemoryBytes = reader.readLine().trim().split(":")(1).replace("kB", "").trim.toLong * 1000
    val totalMemory = new StorageUnit(totalMemBytes).toHuman()
    val freeMemory = new StorageUnit(freeMemoryBytes).toHuman()
    reader.close()
    (totalMemory, freeMemory, ((freeMemoryBytes * 100) / totalMemBytes).toInt)
  }


  /**
   * @return CPU usage of OS
   */
  def getCpuUsage: String = {
    val cmd = Array("/bin/sh", "-c", "top -bn 2 -d 0.01 | grep '^%Cpu' | tail -n 1 | gawk '{print $2+$4+$6}'")
    val process = Runtime.getRuntime.exec(cmd)
    val reader = new BufferedReader(new InputStreamReader(process.getInputStream))
    var cpu = "0"
    try {
       cpu = reader.readLine().trim()
    }catch{
      case e: Exception => cpu = "0"
    }
    reader.close()
    cpu
  }


}