package com.itylos.core.rest.dto

/**
 * DTO that holds weather conditions
 */
case class WeatherConditionsDto(location: String,
                                temperature: Double,
                                temperatureSymbol: String,
                                humidity: Double,
                                humiditySymbol: String
                                 ) {


}
