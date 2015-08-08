package com.itylos.core.service.protocol

import com.itylos.core.domain.{Sensor, SensorEvent}
import com.itylos.core.rest.dto.{WeatherConditionsDto, AlarmStatusDto}


/**
 * Describes the messages needed to send notifications through PushBullet
 */
sealed trait NotificationsProtocol extends Protocol

/**
 * Message to notify for new sensor event
 */
case class NewSensorEventNotification(sensor: Sensor, sensorEvent: SensorEvent,kerberosImages:Option[List[String]]=None) extends NotificationsProtocol

/**
 * Message to notify for changes in alarm status
 */
case class UpdatedAlarmStatusNotification(alarmStatus: AlarmStatusDto) extends NotificationsProtocol

/**
 * Message to notify for triggered alarm
 */
case class AlarmTriggeredNotification() extends NotificationsProtocol

/**
 * Message to notify for changes in weather conditions
 */
case class UpdatedWeatherConditionsNotification(weatherConditions: WeatherConditionsDto) extends NotificationsProtocol
