package com.itylos.core.service

import akka.actor.{ActorSystem, Props}
import akka.testkit._
import com.itylos.controller.StopSystemAfterAll
import com.itylos.core.dao.TestEnvironmentRepos
import com.itylos.core.domain._
import com.itylos.core.exception.{ARMED, CannotArmWithNoEnabledZone, DISARMED}
import com.itylos.core.rest.dto.{AlarmStatusDto, AlarmStatusHistoryDto}
import com.itylos.core.service.protocol._
import org.joda.time.DateTimeUtils
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

/**
 * Specs for [[com.itylos.core.service.AlarmStatusServiceActor]]
 */
class AlarmStatusServiceActorTest extends TestKit(ActorSystem("testActorSystem")) with ImplicitSender
with DefaultTimeout with WordSpecLike with Matchers with TestEnvironmentRepos with StopSystemAfterAll
with BeforeAndAfterEach {

  // Fixed time for easier testing
  DateTimeUtils.setCurrentMillisFixed(1000L)

  // Create the actor to test
  val actorRef = TestActorRef(Props(new AlarmStatusServiceActor() with TestEnvironmentRepos with NotificationsHelper {}))

  // Common variables to all tests
  val zoneId = "zoneId"
  val sensorOId = "sensorOId"
  val zonesStatus = ZoneStatus(zoneId, ENABLED)
  val user = new User(Some("userOid"), "userName", "userEmail", "webPass", "alarmPass")

  // Expected responses
  val zone = new Zone(Some(zoneId), "zName", "zDesc", List(sensorOId), 15L)

  // Since SensorEventServiceActor uses notifier we need to make sure messages get delivered to corresponding actors
  val soundServiceActorProbe = TestProbe()
  val temp1 = TestActorRef(Props(new ForwardActor(soundServiceActorProbe.ref)), "soundServiceActor")
  val webSocketActorProbe = TestProbe()
  val temp2 = TestActorRef(Props(new ForwardActor(webSocketActorProbe.ref)), "webSocketActor")
  val pushBulletServiceActorProbe = TestProbe()
  val temp3 = TestActorRef(Props(new ForwardActor(pushBulletServiceActorProbe.ref)), "pushBulletServiceActor")
  val webHooksServiceActorProbe = TestProbe()
  val temp4 = TestActorRef(Props(new ForwardActor(webHooksServiceActorProbe.ref)), "webHooksServiceActor")

  // Reset DAOs
  override def beforeEach(): Unit = {
    reset(alarmStatusDao)
    reset(alarmStatusHistoryDao)
    reset(userDao)
    reset(zoneStatusDao)
  }

  "An AlarmStatusServiceActor" must {
    "create alarm status if one does not exist" in {
      when(alarmStatusDao.getAlarmStatus).thenReturn(None)
      actorRef ! SetupAlarmStatus()
      verify(alarmStatusDao).save(new AlarmStatus())
    }
    "not create alarm status if one already exists" in {
      when(alarmStatusDao.getAlarmStatus).thenReturn(Some(new AlarmStatus()))
      actorRef ! SetupAlarmStatus()
      verify(alarmStatusDao, times(0)).save(new AlarmStatus())
    }
    "throw exception when trying to arm system and no zone is enabled" in {
      val alarmStatus = AlarmStatus()
      when(alarmStatusDao.getAlarmStatus).thenReturn(Some(alarmStatus))
      when(zoneStatusDao.getAllZonesStatus).thenReturn(List(ZoneStatus(zoneId, DISABLED)))
      intercept[CannotArmWithNoEnabledZone] {
        actorRef.receive(UpdateAlarmStatus(ARMED, "alarmPass", user))
      }
      expectNoMsg()
    }
    "arm system" in {
      val alarmStatus = AlarmStatus()
      when(zoneStatusDao.getAllZonesStatus).thenReturn(List(zonesStatus))
      when(alarmStatusDao.getAlarmStatus).thenReturn(Some(alarmStatus))
      actorRef ! UpdateAlarmStatus(ARMED, "alarmPass", user)
      alarmStatus.timeArmed shouldBe 1000L
      alarmStatus.userIdArmed shouldBe user.oid.get
      alarmStatus.status shouldBe ARMED
      verify(alarmStatusDao).update(alarmStatus)
      val alarmStatusHistory = new AlarmStatusHistory(alarmStatus)
      verify(alarmStatusHistoryDao).save(alarmStatusHistory)
      verifyServicesNotified(new AlarmStatusDto(alarmStatus, user))
      expectMsg(new AlarmStatusRs(new AlarmStatusDto(alarmStatus, user)))
    }
    "disarm system" in {
      val alarmStatus = AlarmStatus()
      when(zoneStatusDao.getAllZonesStatus).thenReturn(List(zonesStatus))
      when(alarmStatusDao.getAlarmStatus).thenReturn(Some(alarmStatus))
      actorRef ! UpdateAlarmStatus(DISARMED, "alarmPass", user)
      alarmStatus.timeDisArmed shouldBe 1000L
      alarmStatus.userIdDisarmed shouldBe user.oid.get
      alarmStatus.status shouldBe DISARMED
      verify(alarmStatusDao).update(alarmStatus)
      val alarmStatusHistory = new AlarmStatusHistory(alarmStatus)
      verify(alarmStatusHistoryDao).save(alarmStatusHistory)
      verifyServicesNotified(new AlarmStatusDto(alarmStatus, user))
      expectMsg(new AlarmStatusRs(new AlarmStatusDto(alarmStatus, user)))
    }
    "disarm system (test reset using mocked alarmStatus)" in {
      val alarmStatus = Mockito.mock(classOf[AlarmStatus])
      when(alarmStatus.status).thenReturn(DISARMED)
      when(zoneStatusDao.getAllZonesStatus).thenReturn(List(zonesStatus))
      when(alarmStatusDao.getAlarmStatus).thenReturn(Some(alarmStatus))
      actorRef ! UpdateAlarmStatus(DISARMED, "alarmPass", user)
      verify(alarmStatusDao).update(alarmStatus)
      verify(alarmStatus).resetAlarmStatus()
      expectMsgAnyClassOf(classOf[AlarmStatusRs])
    }
    "get alarm status history" in {
      val alarmStatus = AlarmStatus()
      alarmStatus.status = ARMED
      alarmStatus.userIdArmed = user.oid.get
      when(alarmStatusHistoryDao.getAlarmStatuses(10, 0)).thenReturn(List(new AlarmStatusHistory(alarmStatus)))
      when(userDao.getUserByObjectId(user.oid.get)).thenReturn(Some(user))
      actorRef ! GetAlarmStatusHistoryRq(10, 0)
      expectMsg(AlarmStatusHistoryRs(List(new AlarmStatusHistoryDto(new AlarmStatusHistory(alarmStatus), user))))
    }
    "get alarm status" in {
      val alarmStatus = AlarmStatus()
      when(alarmStatusDao.getAlarmStatus).thenReturn(Some(alarmStatus))
      actorRef ! GetCurrentAlarmStatusRq()
      expectMsg(new AlarmStatusRs(AlarmStatusDto(alarmStatus.status.toString)))
    }
  }

  def verifyServicesNotified(alarmStatus: AlarmStatusDto): Unit = {
    webSocketActorProbe.expectMsg(Event(new UpdatedAlarmStatusNotification(alarmStatus)))
    pushBulletServiceActorProbe.expectMsg(new UpdatedAlarmStatusNotification(alarmStatus))
    webHooksServiceActorProbe.expectMsg(new UpdatedAlarmStatusNotification(alarmStatus))
    soundServiceActorProbe.expectMsg(new UpdatedAlarmStatusNotification(alarmStatus))
  }

}
