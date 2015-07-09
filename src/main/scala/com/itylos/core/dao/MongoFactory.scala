package com.itylos.core.dao

import com.mongodb.casbah.Imports._
import com.typesafe.config.ConfigFactory

/**
 * The mongo factory that should be used to initiate connections to mongo db
 */
object MongoFactory {

  private val dbServerConfig = ConfigFactory.load().getConfig("mongo")
  private val mongoClientConf = MongoClient(dbServerConfig.getString("host"), dbServerConfig.getInt("port"))
  val mongoClient = mongoClientConf(dbServerConfig.getString("db"))

}