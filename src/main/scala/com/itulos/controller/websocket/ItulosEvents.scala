package com.itulos.controller.websocket

import com.itulos.controller.rest.dto.{SystemOSStatsDto, UserDto, AlarmStatusDto, SensorEventDto}
import com.itulos.controller.service.protocol.AlarmStatusRs
import com.itulos.controller.websocket.ItulosEvents.{SystemStatsMessage, UnmonitoredChangesMessage, NewSensorEventMessage, AlarmStatusUpdateMessage}
import spray.json.DefaultJsonProtocol

/**
 * Describes all events that will be sent through web sockets
 */
object ItulosEvents {

  case class AlarmStatusUpdateMessage(eventType: String = "alarmStatusChanged", message: AlarmStatusRs)

  case class SystemStatsMessage(eventType: String = "systemStats", message: SystemOSStatsDto)

  case class NewSensorEventMessage(eventType: String = "newSensorEvent", message: SensorEventDto)

  case class UnmonitoredChangesMessage(eventType: String = "unmonitoredChangesDetected")

}

object ItulosEventsJsonProtocol extends DefaultJsonProtocol {
  implicit val userDtoFormat = jsonFormat10(UserDto)
  implicit val alarmStatusDtoFormat = jsonFormat2(AlarmStatusDto)
  implicit val alarmStatusRsFormat = jsonFormat1(AlarmStatusRs)
  implicit val alarmStatusUpdateEventFormat = jsonFormat2(AlarmStatusUpdateMessage)
  implicit val sensorEventDtoFormat = jsonFormat7(SensorEventDto)
  implicit val newSensorEventMessageFormat = jsonFormat2(NewSensorEventMessage)
  implicit val unmonitoredChangesMessageFormat = jsonFormat1(UnmonitoredChangesMessage)
  implicit val systemOSStatsFormat = jsonFormat7(SystemOSStatsDto)
  implicit val systemStatsMessageFormat = jsonFormat2(SystemStatsMessage)
}

