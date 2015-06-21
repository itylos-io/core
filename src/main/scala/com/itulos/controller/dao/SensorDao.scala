package com.itulos.controller.dao

import com.itulos.controller.domain.{Sensor, User}
import com.itulos.controller.exception.SensorDoesNotExistException
import com.mongodb.casbah.query.Imports._
import org.joda.time.DateTime

/**
 * Dao for User
 */
trait SensorComponent {
  val sensorDao: SensorDao

  class SensorDao extends CommonDao[User] {
    val COLLECTION_NAME = "sensors"

    override def getCollectionName: String = COLLECTION_NAME

    /**
     * Checks if sensors exists. Filters on sensor id
     * @throws SensorDoesNotExistException if oid does not correspond to sensor
     * @param sensorIds the object ids of the sensors to check
     */
    def checkSensorsExistenceBySensorId(sensorIds:List[String]): Unit ={
      sensorIds.foreach(sensorId => {
        if (getSensorBySensorId(sensorId) == None) {
          throw new SensorDoesNotExistException(sensorId)
        }
      })
    }

    /**
     * Checks if sensors exists. Filters on object id of sensor (oid)
     * @throws SensorDoesNotExistException if oid does not correspond to sensor
     * @param sensorOIds the object ids of the sensors to check
     */
    def checkSensorsExistenceByOid(sensorOIds:List[String]): Unit ={
      sensorOIds.foreach(sensorOId => {
        if (getSensorByObjectId(sensorOId) == None) {
          throw new SensorDoesNotExistException(sensorOId)
        }
      })
    }

    def update(sensor: Sensor): Unit = {

      val query = MongoDBObject("_id" -> new ObjectId(sensor.oid.get))
      val update = $set(
        "name" -> sensor.name,
        "sensorId" -> sensor.sensorId,
        "description" -> sensor.description,
        "location" -> sensor.location,
        "sensorTypeId" -> sensor.sensorTypeId,
        "isActive" -> sensor.isActive,
        "dateRegistered" -> new DateTime().getMillis
      )
      db(getCollectionName).update(query, update, upsert = false)
    }

    def getSensorByObjectId(id: String): Option[Sensor] = {
      val data = getByIDField(id)
      if (data != None) {
        return Some(new Sensor(data.get))
      }
      None
    }

    def getSensorBySensorId(sensorId: String): Option[Sensor] = {
      val data = getByField("sensorId", sensorId)
      if (data != None) {
        return Some(new Sensor(data.get))
      }
      None
    }

    def getAllSensor: List[Sensor] = {
      val data = listAll(Some("dateRegistered"),Some(true))
      if (data.isEmpty) return List()
      data.map(x => new Sensor(x)).toList
    }

    def deleteSensorByObjectId(id: String) = {
      val data = getByIDField(id)
      if (data != None) db(getCollectionName).remove(data.get)
    }


  }

}