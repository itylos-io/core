package com.itylos.core.dao

import org.mockito.Mockito

trait TestEnvironmentRepos extends SensorTypeComponent with TokenDaoComponent
with UserDaoComponent with ZoneStatusComponent with ZoneComponent with SensorComponent
with SettingsComponent with SensorEventComponent with AlarmStatusComponent with AlarmStatusHistoryComponent {

  override val userDao = TestEnvironmentRepos.userDao.asInstanceOf[UserDao]
  override val sensorTypeDao = TestEnvironmentRepos.sensorTypeDao.asInstanceOf[SensorTypeDao]
  override val tokenDao = TestEnvironmentRepos.tokenDao.asInstanceOf[TokenDao]
  override val zoneStatusDao = TestEnvironmentRepos.zoneStatusDao.asInstanceOf[ZoneStatusDao]
  override val zoneDao = TestEnvironmentRepos.zoneDao.asInstanceOf[ZoneDao]
  override val sensorDao = TestEnvironmentRepos.sensorDao.asInstanceOf[SensorDao]
  override val settingsDao = TestEnvironmentRepos.settingsDao.asInstanceOf[SettingsDao]
  override val sensorEventDao = TestEnvironmentRepos.sensorEventDao.asInstanceOf[SensorEventDao]
  override val alarmStatusDao = TestEnvironmentRepos.alarmStatusDao.asInstanceOf[AlarmStatusDao]
  override val alarmStatusHistoryDao = TestEnvironmentRepos.alarmStatusHistoryDao.asInstanceOf[AlarmStatusHistoryDao]

}

object TestEnvironmentRepos extends SensorTypeComponent with TokenDaoComponent
with UserDaoComponent with ZoneStatusComponent with ZoneComponent with SensorComponent
with SettingsComponent with SensorEventComponent with AlarmStatusComponent with AlarmStatusHistoryComponent {

  override val userDao = Mockito.mock(classOf[UserDao])
  override val sensorTypeDao = Mockito.mock(classOf[SensorTypeDao])
  override val tokenDao = Mockito.mock(classOf[TokenDao])
  override val zoneStatusDao = Mockito.mock(classOf[ZoneStatusDao])
  override val zoneDao = Mockito.mock(classOf[ZoneDao])
  override val sensorDao = Mockito.mock(classOf[SensorDao])
  override val settingsDao = Mockito.mock(classOf[SettingsDao])
  override val sensorEventDao = Mockito.mock(classOf[SensorEventDao])
  override val alarmStatusDao = Mockito.mock(classOf[AlarmStatusDao])
  override val alarmStatusHistoryDao = Mockito.mock(classOf[AlarmStatusHistoryDao])

}


