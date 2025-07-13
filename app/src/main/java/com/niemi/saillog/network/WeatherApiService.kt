package com.niemi.saillog.network

import com.niemi.saillog.BuildConfig
import com.niemi.saillog.data.OneCallResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("onecall")
    suspend fun getOneCallWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("exclude") exclude: String? = null,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OneCallResponse

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/data/3.0/"

        fun create(): WeatherApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}

object ApiConfig {
    const val OPENWEATHER_API_KEY = BuildConfig.OPENWEATHER_API_KEY
}