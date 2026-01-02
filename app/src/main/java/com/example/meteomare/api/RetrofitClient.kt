package com.example.meteomare.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object RetrofitClient {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }
    private val contentType = "application/json".toMediaType()

    val weatherApi: OpenMeteoService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(OpenMeteoService::class.java)
    }


    val marineApi: OpenMeteoService by lazy {
        Retrofit.Builder()
            .baseUrl("https://marine-api.open-meteo.com/") // URL specifico per i mari
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(OpenMeteoService::class.java)
    }

}
