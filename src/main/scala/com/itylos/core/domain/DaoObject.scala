package com.itylos.core.domain

import com.mongodb.casbah.Imports


abstract class DaoObject {

  /**
   * @return a representation of this object as Db Object
   */
  def asDbObject(): Imports.DBObject

}
