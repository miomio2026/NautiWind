package com.example.meteomare.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarineResponse(val hourly: MarineHourly? = null)

@Serializable
data class MarineHourly(
    @SerialName("wave_height") val waveHeight: List<Double?> = emptyList()
)

@Serializable
data class WeatherResponse(val hourly: WeatherHourly? = null)

@Serializable
data class WeatherHourly(
    @SerialName("wind_direction_10m") val windDirection: List<Double?> = emptyList(),
    @SerialName("wind_speed_10m") val windSpeed: List<Double?> = emptyList()
)

data class MarePunto(
    val nome: String,
    val regione: String,
    val lat: Double,
    val lon: Double,
    val previsioniOnde: List<Double> = emptyList(),
    val direzioniVento: List<Double> = emptyList(),
    val velocitaVento: List<Double> = emptyList(),
    val xOffset: Int,
    val yOffset: Int,
    val urlWebcam: String? = null
)

