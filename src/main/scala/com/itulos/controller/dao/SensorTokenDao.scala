package com.itulos.controller.dao

import com.itulos.controller.domain.SensorToken

/**
 * Dao for Tokens
 */
trait SensorTokenDaoComponent {
  val sensorTokenDao: SensorTokenDao

  class SensorTokenDao extends CommonDao[SensorToken] {
    val COLLECTION_NAME = "sensorTokens"

    override def getCollectionName: String = COLLECTION_NAME

    def getToken: Option[SensorToken] = {
      val data = listAll()
      if (data.isEmpty) return None
      Some(data.map(x => new SensorToken(x)).toList.head)
    }


  }

}