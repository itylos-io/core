package com.itulos.controller.dao

import org.mockito.Mockito

trait TestEnvironmentRepos extends SensorTokenDaoComponent with SensorTypeComponent with TokenDaoComponent
with UserDaoComponent {

  override val userDao = TestEnvironmentRepos.userDao.asInstanceOf[UserDao]

  override val sensorTokenDao = TestEnvironmentRepos.sensorTokenDao.asInstanceOf[SensorTokenDao]

  override val sensorTypeDao = TestEnvironmentRepos.sensorTypeDao.asInstanceOf[SensorTypeDao]

  override val tokenDao = TestEnvironmentRepos.tokenDao.asInstanceOf[TokenDao]

}

object TestEnvironmentRepos extends SensorTokenDaoComponent with SensorTypeComponent with TokenDaoComponent
with UserDaoComponent {

  override val userDao = Mockito.mock(classOf[UserDao])

  override val sensorTokenDao = Mockito.mock(classOf[SensorTokenDao])

  override val sensorTypeDao = Mockito.mock(classOf[SensorTypeDao])

  override val tokenDao = Mockito.mock(classOf[TokenDao])

}


