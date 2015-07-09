package com.itylos.core.dao

import com.itylos.core.domain.AlarmStatus
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._

/**
 * Dao for EnabledAlarm
 */
trait AlarmStatusComponent {
  val alarmStatusDao: AlarmStatusDao

  class AlarmStatusDao extends CommonDao[AlarmStatus] {
    val COLLECTION_NAME = "alarmStatus"

    override def getCollectionName: String = COLLECTION_NAME

    /**
     * Queries for AlarmStatus
     * @return the AlarmStatus
     */
    def getAlarmStatus: Option[AlarmStatus] = {
      val data = getOne
      if (data == None) None else Some(new AlarmStatus(data.get))
    }

    /**
     * Query for AlarmStatus based on object id
     * @param id the id of the AlarmStatus to query for
     * @return the AlarmStatus if any
     */
    def getAlarmStatusByObjectId(id: String): Option[AlarmStatus] = {
      val data = getByIDField(id)
      if (data != None) {
        return Some(new AlarmStatus(data.get))
      }
      None
    }

    def update(alarmStatus: AlarmStatus): Unit = {

      val query = MongoDBObject("_id" ->  new ObjectId(alarmStatus.oid.get))
      val update = $set(
        "status" -> alarmStatus.status.toString,
        "zoneIds" -> alarmStatus.zoneIds.mkString(","),
        "sensorOIds" -> alarmStatus.sensorOIds.mkString(","),
        "falseEnteredPasswords" -> alarmStatus.falseEnteredPasswords,
        "emailNotificationsSent" -> alarmStatus.emailNotificationsSent,
        "pushBulletNotificationsSent" -> alarmStatus.pushBulletNotificationsSent,
        "smsSent" -> alarmStatus.smsSent,
        "violationTime" -> alarmStatus.violationTime,
        "timeArmed" -> alarmStatus.timeArmed,
        "timeDisArmed" -> alarmStatus.timeDisArmed,
        "userIdArmed" -> alarmStatus.userIdArmed,
        "userIdDisarmed" -> alarmStatus.userIdDisarmed)

      db(getCollectionName).update(query, update, upsert = true)
    }

  }

}