package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao.SensorTypeComponent
import com.itylos.core.domain.SensorType
import com.itylos.core.service.protocol.{GetAllSensorTypesRs, GetAllSensorTypesRq, LoadSensorTypes}
import org.apache.commons.io.IOUtils
import spray.json.JsonParser

/**
 * Companion object to properly initiate [[com.itylos.core.service.SensorTypeServiceActor]]
 */
object  SensorTypeServiceActor {
  def props(): Props = {
    Props(new SensorTypeServiceActor() with SensorTypeComponent {
      val sensorTypeDao = new SensorTypeDao
    })
  }
}

/**
 * An actor responsible for managing [[com.itylos.core.domain.SensorType]]
 */
class SensorTypeServiceActor extends Actor with ActorLogging {
  this: SensorTypeComponent =>

  import com.itylos.core.domain.SensorTypeJsonProtocol._

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