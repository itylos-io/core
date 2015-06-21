package com.itulos.controller.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao.SensorTypeComponent
import com.itulos.controller.domain.SensorType
import com.itulos.controller.service.protocol.{GetAllSensorTypesRs, GetAllSensorTypesRq, LoadSensorTypes}
import org.apache.commons.io.IOUtils
import spray.json.JsonParser

object  SensorTypeServiceActor {
  def props(): Props = {
    Props(new SensorTypeServiceActor() with SensorTypeComponent {
      val sensorTypeDao = new SensorTypeDao
    })
  }
}

/**
 * An actor responsible for managing sensor types
 */
class SensorTypeServiceActor extends Actor with ActorLogging {
  this: SensorTypeComponent =>

  import com.itulos.controller.domain.SensorTypeJsonProtocol._

  def receive = {
    // --- Load sensors metadata if not already loaded --- //
    case LoadSensorTypes() =>
      val jsonFileIn = this.getClass.getClassLoader.getResourceAsStream("sensorTypes.json")
      val configurationString = IOUtils.toString(jsonFileIn, "UTF-8")
      val jsonAst = JsonParser(configurationString)
      val types = jsonAst.convertTo[List[SensorType]]
      types.foreach(t => {
        if (sensorTypeDao.getSensorTypeByObjectId(t.id) == None)
          sensorTypeDao.save(t)
      })
    // --- Get all sensors --- //
    case GetAllSensorTypesRq() =>
      val data = sensorTypeDao.getAllSensorTypes
      sender() ! GetAllSensorTypesRs(data)
  }


}