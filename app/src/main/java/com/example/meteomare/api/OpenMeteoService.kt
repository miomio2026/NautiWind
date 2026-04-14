package com.example.meteomare.api

import com.example.meteomare.data.MarineResponse
import com.example.meteomare.data.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoService {
    @GET("v1/forecast")
    suspend fun getWeatherData(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "wind_direction_10m,wind_speed_10m"
    ): WeatherResponse

    @GET("v1/marine")
    suspend fun getMarineData(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "wave_height"
    ): MarineResponse
}


