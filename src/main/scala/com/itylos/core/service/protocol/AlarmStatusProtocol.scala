package com.itylos.core.service.protocol

import com.itylos.core.domain.{SensorEvent, User}
import com.itylos.core.exception.AlarmStatusEnum
import com.itylos.core.rest.dto.{AlarmStatusHistoryDto, AlarmStatusDto}


/**
 * Describes the messages needed for Alarm management
 */
sealed trait AlarmStatusProtocol extends Protocol

/**
 * Message to setup alarm status for first time
 */
case class SetupAlarmStatus() extends AlarmStatusProtocol

/**
 * Message to change the status of the alarm
 * @param status the new status
 *             @param password the entered password
 * @param user the user performing the action
 */
case class UpdateAlarmStatus(status: AlarmStatusEnum, password:String,user: User) extends AlarmStatusProtocol

/**
 * Message that indicates a new sensor event has occurred
 * @param sensorEvent the sensorEvent
 */
case class NewSensorEvent(sensorEvent: SensorEvent) extends AlarmStatusProtocol

/**
 * Message to check if any of the alarms has been violated (due to timeout of false password entries)
 */
case class CheckEnabledAlarms() extends AlarmStatusProtocol

/**
 * Message to get alarm status history
 */
case class GetAlarmStatusHistoryRq(limit:Int,offset:Int) extends AlarmStatusProtocol

/**
 * Response message to get GetAlarmStatusHistoryRq
 * @param alarmStatuses the alarm status history DTOs
 */
case class AlarmStatusHistoryRs(alarmStatuses: List[AlarmStatusHistoryDto]) extends AlarmStatusProtocol


/**
 * Message to get current alarm status
 */
case class GetCurrentAlarmStatusRq() extends AlarmStatusProtocol

/**
 * Response message to get GetCurrentAlarmStatusRq
 * @param alarmStatus the current alarm status DTO
 */
case class AlarmStatusRs(alarmStatus: AlarmStatusDto) extends AlarmStatusProtocol