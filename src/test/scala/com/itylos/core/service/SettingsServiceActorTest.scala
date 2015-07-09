package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.rest.dto.SettingsDto
import com.itylos.core.service.protocol._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.SettingsServiceActor]]
 */
class SettingsServiceActorTest extends TestKit(ActorSystem("testActorSystem")) with ImplicitSender
with DefaultTimeout with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll
with BeforeAndAfterEach {

  // Create the actor to test
  val actorRef = TestActorRef(Props(new SettingsServiceActor() with TestEnvironmentRepos {}))

  // Common variables to all tests
  val settings = new Settings()

  // Expected responses
  val settingsDto = new SettingsDto(settings)

  // Reset DAOs
  override def beforeEach() {
    reset(settingsDao)
  }

  "A SettingsServiceActor" must {
    "setup initial configuration for settings" in {
      when(settingsDao.getSettings).thenReturn(None)
      actorRef ! SetupInitialSettingRq()
      verify(settingsDao).save(settings)
      expectMsg(GetSystemSettingsRs(settingsDto))
    }
    "not setup initial configuration for settings if settings exist" in {
      when(settingsDao.getSettings).thenReturn(Some(settings))
      actorRef ! SetupInitialSettingRq()
      verify(settingsDao, times(0)).save(settings)
      expectMsg(GetSystemSettingsRs(settingsDto))
    }
    "update nexmo settings" in {
      val newSettings = new Settings()
      val nexmoSettings = NexmoSettings(isEnabled = true, "nexmoKey", "nexmoSecret", List("00306978787877"),"https://rest.nexmo.com/sms/json")
      when(settingsDao.getSettings).thenReturn(Some(newSettings))
      actorRef ! UpdateNexmoSettingsRq(nexmoSettings)
      newSettings.nexmoSettings = nexmoSettings
      verify(settingsDao).deleteSettings()
      verify(settingsDao).save(newSettings)
      expectMsg(GetSystemSettingsRs(new SettingsDto(newSettings)))
    }
    "update email settings" in {
      val newSettings = new Settings()
      val emailSettings = EmailSettings(isEnabled = false, smtpStartTLSEnabled = true,
        "smtp.gmail.com", "email@gmail.com", "123", 587, smtpAuth = true, List("john@smith.com"))
      when(settingsDao.getSettings).thenReturn(Some(newSettings))
      actorRef ! UpdateEmailSettingsRq(emailSettings)
      newSettings.emailSettings = emailSettings
      verify(settingsDao).deleteSettings()
      verify(settingsDao).save(newSettings)
      expectMsg(GetSystemSettingsRs(new SettingsDto(newSettings)))
    }
    "update system settings" in {
      val newSettings = new Settings()
      val systemSettings = SystemSettings(30, 150, 150, playSoundsForTriggeredAlarm = false,
        playSoundsForSensorEvents = true,playSoundsForAlarmStatusUpdates=true, accessToken = "accessToken_ChangeMe!!")
      when(settingsDao.getSettings).thenReturn(Some(newSettings))
      actorRef ! UpdateSystemSettingsRq(systemSettings)
      newSettings.systemSettings = systemSettings
      verify(settingsDao).deleteSettings()
      verify(settingsDao).save(newSettings)
      expectMsg(GetSystemSettingsRs(new SettingsDto(newSettings)))
    }
    "update pushbullet settings" in {
      val newSettings = new Settings()
      val pushBulletDevice = new PushBulletDevice(isEnabled = true, "iden", "desc")
      val pushBulletSettings = PushBulletSettings(isEnabled = true, notifyForSensorEvents = false,
        notifyForAlarms = true, notifyForAlarmsStatusUpdates = true, "accessToken", List(pushBulletDevice),"https://rest.nexmo.com/sms/json")
      when(settingsDao.getSettings).thenReturn(Some(newSettings))
      actorRef ! UpdatePushBulletSettingsRq(pushBulletSettings)
      newSettings.pushBulletSettings = pushBulletSettings
      verify(settingsDao).deleteSettings()
      verify(settingsDao).save(newSettings)
      expectMsg(GetSystemSettingsRs(new SettingsDto(newSettings)))
    }
    "update webhook settings" in {
      val newSettings = new Settings()
      val webHookSettings = new WebHookSettings(isEnabled = true, List("url1"))
      when(settingsDao.getSettings).thenReturn(Some(newSettings))
      actorRef ! UpdateWebHookSettingsRq(webHookSettings)
      newSettings.webHookSettings = webHookSettings
      verify(settingsDao).deleteSettings()
      verify(settingsDao).save(newSettings)
      expectMsg(GetSystemSettingsRs(new SettingsDto(newSettings)))
    }
    "get settings" in {
      val newSettings = new Settings()
      when(settingsDao.getSettings).thenReturn(Some(newSettings))
      actorRef ! GetSystemSettingsRq()
      expectMsg(GetSystemSettingsRs(new SettingsDto(newSettings)))
    }
  }

}
