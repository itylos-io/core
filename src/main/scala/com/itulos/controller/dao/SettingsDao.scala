package com.itulos.controller.dao

import com.itulos.controller.domain.SystemSettings

/**
 * Dao for Settings
 */
trait SettingsComponent {
  val settingsDao: SettingsDao

  class SettingsDao extends CommonDao[SystemSettings] {
    val COLLECTION_NAME = "settings"

    override def getCollectionName: String = COLLECTION_NAME

    def getSettings: Option[SystemSettings] = {
      val data = listAll()
      if (data.isEmpty) return None
      Some(data.map(x => new SystemSettings(x)).toList.head)
    }

    def deleteSettings(): Unit ={
      db(getCollectionName).drop()
    }



  }

}