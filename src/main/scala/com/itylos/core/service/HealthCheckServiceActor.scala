package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao.HealthCheckComponent
import com.itylos.core.domain.HealthCheck
import com.itylos.core.rest.dto.HealthCheckDto
import com.itylos.core.service.protocol._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scalaj.http.{Http, HttpOptions}

/**
 * Companion object to properly initiate [[com.itylos.core.domain.HealthCheck]]
 */
object HealthCheckServiceActor {
  def props(): Props = {
    Props(new HealthCheckServiceActor() with HealthCheckComponent {
      val healthCheckDao = new HealthCheckDao
    })
  }
}

/**
 * An actor responsible for managing [[com.itylos.core.domain.HealthCheck]]
 */
class HealthCheckServiceActor extends Actor with ActorLogging {
  this: HealthCheckComponent =>

  val CHECK_INTERVAL = 5 // seconds


  override def preStart() {
    import scala.concurrent.duration._
    context.system.scheduler.schedule(5.seconds, 15.seconds, self, HealthCheckUrls())
    log.info("Starting Health Check service...")
  }

  def receive = {

    // --- Update the urls to check --- //
    case UpdateHealthCheckUrls(urls) =>
      healthCheckDao.removeAllHealthChecks()
      urls.foreach(url => {
        val healthCheck = new HealthCheck(None, url, 0, 0, CHECK_INTERVAL)
        healthCheckDao.save(healthCheck)
      })
      sender() ! GetHealthChecksRs(convert2DTOs(healthCheckDao.getAllHealthChecks))

    // --- Health check configured urls --- //
    case HealthCheckUrls() =>
      healthCheckDao.getAllHealthChecks.foreach(healthCheck => {
        try {
          val rs = Http(healthCheck.url).header("Content-Type", "application/json").header("Charset", "UTF-8")
            .option(HttpOptions.readTimeout(10000)).asString
          healthCheck.lastCheckStatusCode = rs.code
        } catch {
          case e: Exception => healthCheck.lastCheckStatusCode = -1
        }
        healthCheck.lastTimeChecked = new DateTime().getMillis
        healthCheckDao.update(healthCheck)
      })

    // --- Get all configured health checks --- //
    case GetHealthChecksRq() =>
      sender() ! GetHealthChecksRs(convert2DTOs(healthCheckDao.getAllHealthChecks))

  }

  /**
   * Convert [[com.itylos.core.domain.HealthCheck]] to [[com.itylos.core.rest.dto.HealthCheckDto]]
   */
  private def convert2DTOs(healthChecks: List[HealthCheck]): List[HealthCheckDto] = {
    for (healthCheck <- healthChecks) yield {
      new HealthCheckDto(healthCheck)
    }
  }


}