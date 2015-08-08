package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao.{KerberosEventImagesComponent, SensorComponent, SensorEventComponent, SensorTypeComponent}
import com.itylos.core.domain.SensorEvent
import com.itylos.core.exception.{SensorDoesNotExistException, SensorTypeDoesNotExistException}
import com.itylos.core.rest.dto.SensorEventDto
import com.itylos.core.service.protocol._

import scala.collection.mutable.ListBuffer

/**
 * Companion object to properly initiate [[com.itylos.core.service.SensorEventServiceActor]]
 */
object SensorEventServiceActor {
  def props(): Props = {
    Props(new SensorEventServiceActor() with SensorEventComponent with SensorComponent
      with SensorTypeComponent with KerberosEventImagesComponent with NotificationsHelper {
      val sensorDao = new SensorDao
      val sensorEventDao = new SensorEventDao
      val sensorTypeDao = new SensorTypeDao()
      val kerberosEventImagesDao = new KerberosEventImagesDao
    })
  }
}

/**
 * An actor responsible for managing [[com.itylos.core.domain.SensorEvent]]
 */
class SensorEventServiceActor extends Actor with ActorLogging {
  this: SensorEventComponent with SensorComponent with SensorTypeComponent with KerberosEventImagesComponent
    with NotificationsHelper =>

  val MAX_SENSOR_EVENTS_TO_STORE_PER_SENSOR = 20
  val KERBEROS_SENSOR_TYPE_ID = "5"

  def receive = {

    // --- Add sensor event --- //
    case AddSensorEventRq(sensorEvent) =>
      // Check sensor existence
      val sensorData = sensorDao.getSensorBySensorId(sensorEvent.sensorId)
      if (sensorData == None) throw new SensorDoesNotExistException(sensorEvent.sensorId)
      // Check sensor type
      val sensorType = sensorTypeDao.getSensorTypeByObjectId(sensorData.get.sensorTypeId)
      if (sensorType == None) throw new SensorTypeDoesNotExistException(sensorData.get.sensorTypeId)
      if (!sensorType.get.isBatteryPowered) sensorEvent.batteryLevel = -1
      sensorEventDao.save(sensorEvent)
      removePastEvents(sensorEvent.sensorId)
      val kerberosEventImages = if (sensorData.get.sensorTypeId == KERBEROS_SENSOR_TYPE_ID)
        Some(kerberosEventImagesDao.getImagesForKerberosEvent(sensorEvent.kerberosEventId.get).get.imagesUrls)
      else None
      notifyAll(context, NewSensorEventNotification(sensorData.get, sensorEvent, kerberosEventImages))
      sender ! GetSensorEventsRs(List())

    // --- Get sensor events --- //
    case GetSensorEventsRq(sensorId, limit, offset) =>
      val latestEvents = sensorEventDao.getSensorEvents(sensorId, limit, offset)
      sender ! GetSensorEventsRs(convert2DTOs(latestEvents))

    // --- Get latest event for each sensor --- //
    case GetSensorLatestEventsRq(sensorIds) =>
      val latestEvents = for (sensorId <- sensorIds) yield sensorEventDao.getLatestSensorEvent(sensorId)
      val rs = latestEvents.filter(le => le != None).map(le => le.get)
      sender ! GetSensorEventsRs(convert2DTOs(rs))

    // --- Remove sensor events associated to a sensor --- //
    case RemoveSensorEventsForSensor(sensorOId) =>
      sensorDao.checkSensorsExistenceByOid(List(sensorOId))
      val sensorId = sensorDao.getSensorByObjectId(sensorOId).get.sensorId
      sensorEventDao.removeEventsForSensor(sensorId)
      val latestEvents = sensorEventDao.getSensorEvents(None, 5, 0)
      sender ! GetSensorEventsRs(convert2DTOs(latestEvents))

  }

  /**
   * Removes events from sensor that have length > 100
   */
  private def removePastEvents(sensorId: String): Unit = {
    val sensors = sensorEventDao.getSensorEvents(Some(sensorId), 1000, 0)
    if (sensors.length > MAX_SENSOR_EVENTS_TO_STORE_PER_SENSOR) {
      val toRemove = sensors.reverse.slice(MAX_SENSOR_EVENTS_TO_STORE_PER_SENSOR, sensors.length)
      toRemove.foreach(tr => sensorEventDao.removeEventBySensorEventId(tr.oid.get))
    }

  }

  /**
   * Convert [[com.itylos.core.domain.SensorEvent]] to [[com.itylos.core.rest.dto.SensorEventDto]]
   * @param sensorEvents the [[com.itylos.core.domain.SensorEvent]] instances to convert
   * @return the converted DTO objects
   */
  private def convert2DTOs(sensorEvents: List[SensorEvent]): List[SensorEventDto] = {

    var sensorEventsAsDTOs = new ListBuffer[SensorEventDto]()
    for (sensorEvent <- sensorEvents)  {
      val sensor = sensorDao.getSensorBySensorId(sensorEvent.sensorId)

      // sensor has been deleted or renamed? We need to delete those sensor events...
      if (sensor == None) {
        sensorEventDao.removeEventsForSensor(sensorEvent.sensorId)
      }else{
        // If it is a kerberos sensor, fetch associated images
        val kerberosEventImages = if (sensor.get.sensorTypeId == KERBEROS_SENSOR_TYPE_ID)
          Some(kerberosEventImagesDao.getImagesForKerberosEvent(sensorEvent.kerberosEventId.get).get.imagesUrls)
        else None
        sensorEventsAsDTOs += new SensorEventDto(sensorEvent, sensor.get, kerberosEventImages)
      }
    }
    sensorEventsAsDTOs.toList
  }


}