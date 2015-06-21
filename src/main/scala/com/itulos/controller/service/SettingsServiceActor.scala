package com.itulos.controller.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itulos.controller.dao.SettingsComponent
import com.itulos.controller.domain.SystemSettings
import com.itulos.controller.rest.dto.SettingsDto
import com.itulos.controller.service.protocol.{GetSystemSettingsRq, GetSystemSettingsRs, SetupInitialSettingRq, UpdateSystemSettingsRq}

object SettingsServiceActor {
  def props(): Props = {
    Props(new SettingsServiceActor() with SettingsComponent {
      val settingsDao = new SettingsDao
    })
  }
}

/**
 * An actor responsible for managing SystemSettings
 */
class SettingsServiceActor extends Actor with ActorLogging {
  this: SettingsComponent =>


  def receive = {
    // --- Setup initial settings --- //
    case SetupInitialSettingRq() =>
      val settings = settingsDao.getSettings
      if (settings == None) settingsDao.save(SystemSettings())
      sender() ! GetSystemSettingsRs(getSettingsAsDto)
    // --- Update settings --- //
    case UpdateSystemSettingsRq(settings) =>
      settingsDao.deleteSettings()
      settingsDao.save(settings)
      sender() ! GetSystemSettingsRs(getSettingsAsDto)
    // --- Get settings --- //
    case GetSystemSettingsRq() =>
      sender() ! GetSystemSettingsRs(getSettingsAsDto)

  }

  def getSettingsAsDto: SettingsDto ={
  val  settings = settingsDao.getSettings
   if (settings == None) SettingsDto() else new SettingsDto(settings.get)
  }


}