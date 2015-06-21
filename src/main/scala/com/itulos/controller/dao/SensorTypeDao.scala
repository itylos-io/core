package com.itulos.controller.dao

import com.itulos.controller.domain.{SensorType, User}
import com.itulos.controller.exception.{SensorTypeDoesNotExistException, SensorDoesNotExistException}

/**
 * Dao for User
 */
trait SensorTypeComponent {
  val sensorTypeDao: SensorTypeDao

  class SensorTypeDao extends CommonDao[User] {
    val COLLECTION_NAME = "sensorTypes"

    override def getCollectionName: String = COLLECTION_NAME

    /**
     * Checks if sensors types exists. Filters on type id
     * @throws SensorTypeDoesNotExistException if oid does not correspond to sensor type
     * @param typeIds the object ids of the sensors to check
     */
    def checkSensorTypesExistenceBySensorId(typeIds:List[String]): Unit ={
      typeIds.foreach(typeId => {
        if (getSensorTypeByObjectId(typeId) == None) {
          throw new SensorTypeDoesNotExistException(typeId)
        }
      })
    }

    /**
     * Queries for SensorType by object id
     * @param oid the object id of the sensor type
     * @return the SensorType if any
     */
    def getSensorTypeByObjectId(oid: String): Option[SensorType] = {
      val data = getByField("_id", oid)
      if (data != None) {
        return Some(new SensorType(data.get))
      }
      None
    }

    /**
     * Queries for all the sensor types
     * @return the SensorType
     */
    def getAllSensorTypes: List[SensorType] = {
      val data = listAll()
      if (data.isEmpty) return List()
      data.map(x => new SensorType(x)).toList
    }


  }

}