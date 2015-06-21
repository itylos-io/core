package com.itulos.controller.service.protocol


/**
 * Describes the messages needed for Email Service Actor
 */
sealed trait EmailProtocol extends Protocol

/**
 * Message that indicates an email should be sent due to alarm violation
 */
case class NotifyForAlarmViolation() extends EmailProtocol