package com.itylos.core.websocket

import com.itylos.core.rest.dto._
import com.itylos.core.websocket.ItylosNotificationEvents._
import spray.json.DefaultJsonProtocol

/**
 * Describes all events that will be sent through web sockets or through webHooks.
 * Here we define the json payload to be sent in each case
 */
object ItylosNotificationEvents {

  case class UpdatedAlarmStatusMessage(eventType: String = "updatedAlarmStatus", message: AlarmStatusDto)

  case class SystemStatsMessage(eventType: String = "systemStats", message: SystemOSStatsDto)

  case class NewSensorEventMessage(eventType: String = "newSensorEvent", message: SensorEventDto)

  case class UpdatedWeatherConditionsMessage(eventType: String = "updatedWeatherConditions", message: WeatherConditionsDto)

  case class AlarmTriggeredMessage(eventType: String = "alarmTriggered")

}

object ItylosEventsJsonProtocol extends DefaultJsonProtocol {
  implicit val userDtoFormat = jsonFormat9(UserDto)
  implicit val alarmTriggeredMessageFormat = jsonFormat1(AlarmTriggeredMessage)
  implicit val alarmStatusDtoFormat = jsonFormat2(AlarmStatusDto)
  implicit val alarmStatusUpdateEventFormat = jsonFormat2(UpdatedAlarmStatusMessage)
  implicit val sensorEventDtoFormat = jsonFormat9(SensorEventDto)
  implicit val newSensorEventMessageFormat = jsonFormat2(NewSensorEventMessage)
  implicit val systemOSStatsFormat = jsonFormat7(SystemOSStatsDto)
  implicit val systemStatsMessageFormat = jsonFormat2(SystemStatsMessage)
  implicit val weatherConditionsMessageDtoFormat = jsonFormat5(WeatherConditionsDto)
  implicit val weatherConditionsEventFormat = jsonFormat2(UpdatedWeatherConditionsMessage)
}

