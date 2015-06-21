package com.itulos.controller.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao.SensorTokenDaoComponent
import com.itulos.controller.rest.dto.SensorTokenDto
import com.itulos.controller.service.protocol._

object SensorTokenServiceActor {
  def props(): Props = {
    Props(new SensorTokenServiceActor() with SensorTokenDaoComponent {
      val sensorTokenDao = new SensorTokenDao
    })
  }
}

/**
 * An actor responsible for managing tokens
 */
class SensorTokenServiceActor extends Actor with ActorLogging {
  this: SensorTokenDaoComponent =>

  def receive = {
    //--- Update the sensor token --- //
    case UpdateSensorToken(token, forceUpdate) =>
      // If no force update and token exists, do not update
      if (forceUpdate) {
        sensorTokenDao.drop()
        sensorTokenDao.save(token)
      } else {
        if (sensorTokenDao.getToken == None) sensorTokenDao.save(token)
      }
      val sensorToken = sensorTokenDao.getToken
      val tokenDto = if (sensorToken != None) new SensorTokenDto() else new SensorTokenDto(sensorTokenDao.getToken.get)
      sender() ! GetSensorTokenRs(tokenDto)

    //--- Get the sensor token --- //
    case GetSensorTokenRq() =>
      val sensorToken = sensorTokenDao.getToken
      val tokenDto = if (sensorToken != None) new SensorTokenDto() else new SensorTokenDto(sensorTokenDao.getToken.get)
      sender() ! GetSensorTokenRs(tokenDto)
  }


}