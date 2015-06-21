package com.itulos.controller.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao.UserActionDaoComponent
import com.itulos.controller.domain.UserAction
import com.itulos.controller.service.protocol.LogUserAction

object UserActionServiceActor {
  def props(): Props = {
    Props(new UserActionServiceActor() with UserActionDaoComponent {
      val userActionDao = new UserActionDao
    })
  }
}

/**
 * An actor responsible for managing user's actions
 */
class UserActionServiceActor extends Actor with ActorLogging {
  this: UserActionDaoComponent =>


  def receive = {
    // --- Log user's action --- //
    case LogUserAction(user, action, date, ip) =>
      val userAction = UserAction(user.oid.get, date, action, ip)
      userActionDao.save(userAction)

  }


}