package com.itulos.controller.dao

/**
 * Dao for UserActionDao
 */
trait UserActionDaoComponent {
  val userActionDao: UserActionDao

  class UserActionDao extends CommonDao[UserActionDao] {
    val COLLECTION_NAME = "usersActions"

    override def getCollectionName: String = COLLECTION_NAME


  }

}