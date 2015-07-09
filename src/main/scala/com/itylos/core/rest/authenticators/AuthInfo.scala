package com.itylos.core.rest.authenticators

import com.itylos.core.domain.User

class AuthInfo(val user: User) {
  def hasPermission(permission: String) : Boolean ={
    // Code to verify whether user has the given permission      }
    true

  }
}