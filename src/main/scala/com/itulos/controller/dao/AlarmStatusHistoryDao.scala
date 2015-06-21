package com.itulos.controller.dao

import com.itulos.controller.domain.AlarmStatusHistory

/**
 * Dao for AlarmStatusHistory 
 */
trait AlarmStatusHistoryComponent {
  val alarmStatusHistoryDao: AlarmStatusHistoryDao


  class AlarmStatusHistoryDao extends CommonDao[AlarmStatusHistory] {
    val COLLECTION_NAME = "alarmStatusHistory"

    val DATE_SORT_FIELD = Some("dateCreated")
    val DATE_SORT_ASC = Some(false)

    override def getCollectionName: String = COLLECTION_NAME

    /**
     * Get sensor events
     * @param limit how many to return
     * @param offset how many to skip
     * @return the filtered SensorEvent if any
     */
    def getAlarmStatuses(limit: Int, offset: Int): List[AlarmStatusHistory] = {
      val data = listAll(DATE_SORT_FIELD, DATE_SORT_ASC, limit, offset)
      if (data.isEmpty) List() else data.map(x => new AlarmStatusHistory(x)).toList
    }
  }


}