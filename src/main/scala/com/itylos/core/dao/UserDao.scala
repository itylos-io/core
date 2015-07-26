package com.itylos.core.dao


import com.itylos.core.domain.{Sensor, User}
import com.mongodb.casbah.query.Imports._
import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
 * Dao for User
 */
trait UserDaoComponent {
  val userDao: UserDao

  class UserDao extends CommonDao[User] {
    val COLLECTION_NAME = "users"

    override def getCollectionName: String = COLLECTION_NAME

    /**
     * Queries for user based on object id
     * @param oid the object id of the user
     * @return the User if any
     */
    def getUserByObjectId(oid: String): Option[User] = {
      val data = if (oid == "1") getByField("_id", oid) else getByIDField(oid) // 1 is the super admin id
      if (data != None) {
        return Some(new User(data.get))
      }
      None
    }

    /**
     * Queries for user based on email
     * @param email the email of the user
     * @return the User if any
     */
    def getUserByEmail(email: String): Option[User] = {
      val data = getByField("email", email)
      if (data != None) {
        return Some(new User(data.get))
      }
      None
    }

    /**
     * Queries for all users
     * @return the Users
     */
    def getAllUsers: List[User] = {
      val data = listAll(Some("dateRegistered"),Some(true))
      if (data.isEmpty) return List()
      data.map(x => new User(x)).toList
    }

    /**
     * Delete user data based on oid
     * @param oid the object id of the zone to delete
     */
    def deleteUserByObjectId(oid: String) {
      val data = getByIDField(oid)
      if (data != None) db(getCollectionName).remove(data.get)
    }


    /**
     * Update a user
     * @param user
     */
    def update(user: User): Unit = {
      val query = MongoDBObject("_id" -> new ObjectId(user.oid.get))
      val update = $set(
        "name" -> user.name,
        "email" -> user.email,
        "webPassword" -> user.webPassword,
        "alarmPassword" -> user.alarmPassword,
        "isAdmin" -> user.isAdmin,
        "dateRegistered" -> new DateTime().getMillis
      )
      db(getCollectionName).update(query, update, upsert = false)
    }


  }

}