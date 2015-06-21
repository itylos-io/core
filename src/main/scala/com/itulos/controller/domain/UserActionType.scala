package com.itulos.controller.domain


/**
 * Works as a Marker Trait. Each implementation corresponds to an action type.
 */
trait UserActionType {

}

object UserActionType {
  def fromString(action: String): UserActionType = {
    if (action.equalsIgnoreCase(LOGIN.toString)) {
      LOGIN
    } else if (action.equalsIgnoreCase(LOGOUT.toString)) {
      LOGOUT
    } else {
      throw new RuntimeException("No user action available available for " + action)
    }

  }
}

object LOGIN extends UserActionType {
  override def toString: String = {
    "login"
  }
}


object LOGOUT extends UserActionType {
  override def toString: String = {
    "logout"
  }
}
