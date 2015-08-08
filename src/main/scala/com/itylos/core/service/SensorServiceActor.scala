package com.itylos.core.service

import akka.actor.{Actor, ActorLogging, Props}
import com.itylos.core.dao.{SensorComponent, SensorTypeComponent, ZoneComponent, ZoneStatusComponent}
import com.itylos.core.domain.Sensor
import com.itylos.core.exception.SensorIdAlreadyExistsException
import com.itylos.core.rest.dto.{SensorDto, ZoneDto}
import com.itylos.core.service.protocol._
import org.joda.time.DateTime

/**
 * Companion object to properly initiate [[com.itylos.core.service.SensorServiceActor]]
 */
object SensorServiceActor {
  def props(): Props = {
    Props(new SensorServiceActor() with SensorComponent with SensorTypeComponent
      with ZoneComponent with ZoneStatusComponent {
      val sensorDao = new SensorDao
      val sensorTypeDao = new SensorTypeDao
      val zoneDao = new ZoneDao
      val zoneStatusDao = new ZoneStatusDao
    })
  }
}

/**
 * An actor responsible for managing [[com.itylos.core.domain.Sensor]]
 */
class   SensorServiceActor extends Actor with ActorLogging {
  this: SensorComponent with SensorTypeComponent with ZoneComponent with ZoneStatusComponent =>

  val KERBEROS_SENSOR_TYPE_ID = "5"

  def receive = {

    // --- Create sensor --- //
    case CreateSensorRq(sensor) =>
      log.info("Creating new sensor with id [{}]", sensor.sensorId)
      if (sensorDao.getSensorBySensorId(sensor.sensorId) != None) throw new SensorIdAlreadyExistsException(sensor.sensorId)
      sensorTypeDao.checkSensorTypesExistenceBySensorId(List(sensor.sensorTypeId))
      sensorDao.save(sensor)
      sender() ! GetAllSensorRs(convert2DTOs(sensorDao.getAllSensor))

    // --- Update sensor --- //
    case UpdateSensorRq(sensor) =>
      log.info("Updating sensor with oid [{}]", sensor.oid)
      // Check if we try to update a sensor and giving the updated sensor an existing id
      val existingSensor = sensorDao.getSensorBySensorId(sensor.sensorId)
      if (existingSensor != None && existingSensor.get.oid != sensor.oid) throw new SensorIdAlreadyExistsException(sensor.sensorId)
      sensorDao.update(sensor)
      sender() ! GetAllSensorRs(convert2DTOs(sensorDao.getAllSensor))

    // --- Update kerberos sensors --- //
    case UpdateKerberosSensors(instances) =>
      val kerberosSensors = sensorDao.getAllSensor.filter(s => s.sensorTypeId == KERBEROS_SENSOR_TYPE_ID)
      // Ensure all kerberos instances have the corresponding sensor
      instances.foreach(instance => {
        val sensor = kerberosSensors.filter(ks => ks.sensorId == instance.instanceName)
        // create sensor since kerberos instance does not have the corresponding sensor
        if (sensor.isEmpty) {
          val newSensor = Sensor(None, instance.instanceName, instance.instanceName, instance.ip,
            instance.ip, KERBEROS_SENSOR_TYPE_ID, isActive = true, new DateTime().getMillis)
          self ! CreateSensorRq(newSensor)
        }
      })
      // Ensure all sensors match to a kerberos instances
      kerberosSensors.foreach(ks => {
        val instance = instances.filter(i => i.instanceName == ks.sensorId)
        // delete sensor since it does not match to a running kerberos instance
        if (instance.isEmpty) {
          self ! DeleteSensorRq(ks.oid.get)
          context.actorOf(SensorEventServiceActor.props()) ! RemoveSensorEventsForSensor(ks.oid.get)
          context.actorOf(ZoneServiceActor.props()) ! RemoveSensorFromZone(ks.oid.get)
        }
      })

    // --- Delete a sensor --- //
    case DeleteSensorRq(id) =>
      log.info("Deleting sensor with oid [{}]", id)
      sensorDao.deleteSensorByObjectId(id)
      sender() ! GetAllSensorRs(convert2DTOs(sensorDao.getAllSensor))

    // --- Get all sensors --- //
    case GetAllSensorsRq() =>
      sender() ! GetAllSensorRs(convert2DTOs(sensorDao.getAllSensor))
  }

  /**
   * Convert [[com.itylos.core.domain.Sensor]] to [[com.itylos.core.rest.dto.SensorDto]]
   * @param sensors the [[com.itylos.core.domain.Sensor]] objects to convert
   * @return the converted DTO objects
   */
  private def convert2DTOs(sensors: List[Sensor]): List[SensorDto] = {
    for (sensor <- sensors) yield {
      val zones = zoneDao.getZonesForSensorOid(sensor.oid.get)
      val zoneDtos = zones.map(zone => new ZoneDto(zone, zoneStatusDao.getZoneStatusByZoneId(zone.oid.get).get, None))
      new SensorDto(sensor, sensorTypeDao.getSensorTypeByObjectId(sensor.sensorTypeId).get, Some(zoneDtos))
    }
  }


}