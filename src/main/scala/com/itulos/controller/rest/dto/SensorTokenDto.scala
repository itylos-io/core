package com.itulos.controller.rest.dto

import com.itulos.controller.domain.SensorToken
import org.joda.time.{DateTimeZone, DateTime}

/**
 * DTO for SensorToken
 */
case class SensorTokenDto(oid: String,
                     token:String,
                     dateRegistered: String) {

  def this(){
    this("-1","-1","")
  }

  /**
   * Constructor with a SensorToken
   * @param sensorToken the SensorToken to get data from
   */
  def this(sensorToken: SensorToken) {
    this(
      sensorToken.oid.get,
      sensorToken.token,
      new DateTime().withMillis(sensorToken.dateCreated).withZone(DateTimeZone.UTC).toString
    )
  }

}
