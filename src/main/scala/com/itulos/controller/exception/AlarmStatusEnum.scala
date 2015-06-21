package com.itulos.controller.exception

/**
 * Works as a Marker Trait. Each implementation corresponds to a status ARMED/DISARMED
 */
trait AlarmStatusEnum {

}

object AlarmStatusEnum {
  def from(value: String): AlarmStatusEnum = {
    if (value.equalsIgnoreCase(ARMED.toString) )  {
      ARMED
    } else if (value.equalsIgnoreCase(DISARMED.toString)) {
      DISARMED
    } else {
      throw new RuntimeException("No status available for value " + value)
    }
  }
}

object ARMED extends AlarmStatusEnum {
  override def toString: String = {
    "ARMED"
  }
}

object DISARMED extends AlarmStatusEnum {
  override def toString: String = {
    "DISARMED"
  }
}
