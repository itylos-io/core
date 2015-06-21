package com.itulos.controller.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao.{SensorComponent, SensorEventComponent}
import com.itulos.controller.domain.SensorEvent
import com.itulos.controller.rest.dto.SensorEventDto
import com.itulos.controller.service.protocol._
import org.joda.time.DateTime

object SensorEventServiceActor {
  def props(): Props = {
    Props(new SensorEventServiceActor() with SensorEventComponent with SensorComponent with WebSocketNotifier {
      val sensorDao = new SensorDao
      val sensorEventDao = new SensorEventDao
    })
  }
}

/**
 * An actor responsible for managing sensor events
 */
class SensorEventServiceActor extends Actor with ActorLogging {
  this: SensorEventComponent with SensorComponent with WebSocketNotifier =>


  def receive = {

    // --- Add sensor event --- //
    case AddSensorEventRq(sensorEvent) =>
      // Check sensor existence
      sensorDao.checkSensorsExistenceBySensorId(List(sensorEvent.sensorId))
      val latestEvent = sensorEventDao.getLatestSensorEvent(sensorEvent.sensorId)
      // Check if it is a repeated request with same data
      if (latestEvent != None
        && latestEvent.get.status == sensorEvent.status
        && new DateTime().getMillis - latestEvent.get.dateOfEvent < 2000) {
        sender ! GetSensorEventsRs(List())
      } else {
        context.actorOf(SoundServiceActor.props()) ! NewSensorEvent(sensorEvent)
        sensorEventDao.save(sensorEvent)
        val latestEvents = convert2DTOs(sensorEventDao.getSensorEvents(None, 5, 0))
        sender ! GetSensorEventsRs(List())
        notifyWebSocket(context, Event(GetSensorEventsRs(latestEvents)))
      }

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
      sensorDao.checkSensorsExistenceBySensorId(List(sensorOId))
      val sensorId = sensorDao.getSensorBySensorId(sensorOId).get.sensorId
      sensorEventDao.removeEventsForSensor(sensorId)
      val latestEvents = sensorEventDao.getSensorEvents(None, 5, 0)
      sender ! GetSensorEventsRs(convert2DTOs(latestEvents))

  }

  /**
   * Convert SensorEvent to SensorEventDto
   * @param sensorEvents the sensorEvents to convert
   * @return the converted DTO objects
   */
  private def convert2DTOs(sensorEvents: List[SensorEvent]): List[SensorEventDto] = {
    for (sensorEvent <- sensorEvents)
    yield new SensorEventDto(sensorEvent, sensorDao.getSensorBySensorId(sensorEvent.sensorId).get)
  }


}