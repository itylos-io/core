package com.itylos.core.dao

import com.itylos.core.domain.{Settings, SystemSettings}

/**
 * Dao for Settings
 */
trait SettingsComponent {
  val settingsDao: SettingsDao

  class SettingsDao extends CommonDao[SystemSettings] {
    val COLLECTION_NAME = "settings"

    override def getCollectionName: String = COLLECTION_NAME

    def getSettings: Option[Settings] = {
      val data = listAll()
      if (data.isEmpty) return None
      Some(data.map(x => new Settings(x)).toList.head)
    }

    def deleteSettings(): Unit ={
      db(getCollectionName).drop()
    }



  }

}