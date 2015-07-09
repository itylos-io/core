package com.itylos.core.domain

/**
 * Works as a Marker Trait. Each implementation corresponds to a status ENABLED/DISABLED
 */
trait ZoneStatusEnum {

}

object ZoneStatusEnum {
  def from(value: String): ZoneStatusEnum = {
    if (value.equalsIgnoreCase(DISABLED.toString) )  {
      DISABLED
    } else if (value.equalsIgnoreCase(ENABLED.toString)) {
      ENABLED
    } else {
      throw new RuntimeException("No status available for value " + value)
    }
  }
}

object DISABLED extends ZoneStatusEnum {
  override def toString: String = {
    "DISABLED"
  }
}

object ENABLED extends ZoneStatusEnum {
  override def toString: String = {
    "ENABLED"
  }
}
