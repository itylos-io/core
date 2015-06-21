package com.itulos.controller.domain

/**
 * Works as a Marker Trait. Each implementation corresponds to a status OPEN/CLOSED
 */
trait SensorStatus {

}

object SensorStatus {
  def from(value: String): SensorStatus = {
    if (value.equalsIgnoreCase(OPEN.toString) )  {
      OPEN
    } else if (value.equalsIgnoreCase(CLOSED.toString)) {
      CLOSED
    } else {
      throw new RuntimeException("No status available available for value " + value)
    }
  }
}

object OPEN extends SensorStatus {
  override def toString: String = {
    "OPEN"
  }
}

object CLOSED extends SensorStatus {
  override def toString: String = {
    "CLOSED"
  }
}
