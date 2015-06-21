package com.itulos.controller.dao

import com.itulos.controller.domain.Token
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Dao for Tokens
 */
trait TokenDaoComponent {
  val tokenDao: TokenDao

  class TokenDao extends CommonDao[TokenDao] {
    val COLLECTION_NAME = "tokens"

    override def getCollectionName: String = COLLECTION_NAME

    /**
     * Queries for token based on token value
     * @param token the token to query for
     * @return the Token if any
     */
    def getToken(token: String): Option[Token] = {
      val data = getByField("token", token)
      if (data != None) {
        return Some(new Token(data.get))
      }
      None
    }

    /**
     * Delete token based on given token
     * @param token the token to delete
     */
    def deleteToken(token: String) {
      removeByField("token", token)
    }

    /**
     * Remove for all tokens before given time
     * @param time the time for which a token is considered expired
     */
    def deleteExpiredTokens(time: Long) {
      val q: DBObject = "expireTime" $lt time
      db(getCollectionName).remove(q)
    }


  }

}