package com.itulos.controller.rest.authenticators

import com.itulos.controller.domain.User

class AuthInfo(val user: User) {
  def hasPermission(permission: String) : Boolean ={
    // Code to verify whether user has the given permission      }
    true

  }
}