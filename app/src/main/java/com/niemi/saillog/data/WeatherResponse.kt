package com.niemi.saillog.data

import com.google.gson.annotations.SerializedName
import java.util.Date

// OneCall API 3.0 response models
data class OneCallResponse(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    @SerializedName("timezone_offset") val timezoneOffset: Int,
    val current: CurrentWeather,
    val minutely: List<MinutelyWeather>? = null,
    val hourly: List<HourlyWeather>? = null,
    val daily: List<DailyWeather>? = null,
    val alerts: List<OpenWeatherAlert>? = null
)

data class CurrentWeather(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    val pressure: Int,
    val humidity: Int,
    @SerializedName("dew_point") val dewPoint: Double,
    val uvi: Double,
    val clouds: Int,
    val visibility: Int,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("wind_deg") val windDeg: Int,
    @SerializedName("wind_gust") val windGust: Double? = null,
    val weather: List<Weather>
)

data class MinutelyWeather(
    val dt: Long,
    val precipitation: Double
)

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    val pressure: Int,
    val humidity: Int,
    @SerializedName("dew_point") val dewPoint: Double,
    val uvi: Double,
    val clouds: Int,
    val visibility: Int,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("wind_deg") val windDeg: Int,
    @SerializedName("wind_gust") val windGust: Double? = null,
    val weather: List<Weather>,
    val pop: Double // Probability of precipitation
)

data class DailyWeather(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val moonrise: Long,
    val moonset: Long,
    @SerializedName("moon_phase") val moonPhase: Double,
    val summary: String? = null,
    val temp: Temperature,
    @SerializedName("feels_like") val feelsLike: FeelsLike,
    val pressure: Int,
    val humidity: Int,
    @SerializedName("dew_point") val dewPoint: Double,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("wind_deg") val windDeg: Int,
    @SerializedName("wind_gust") val windGust: Double? = null,
    val weather: List<Weather>,
    val clouds: Int,
    val pop: Double,
    val rain: Double? = null,
    val uvi: Double
)

data class Temperature(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class FeelsLike(
    val day: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)


// Renamed to avoid conflict with UI WeatherAlert
data class OpenWeatherAlert(
    @SerializedName("sender_name") val senderName: String,
    val event: String,
    val start: Long,
    val end: Long,
    val description: String,
    val tags: List<String>
)

// Enhanced sailing weather data
data class SailingWeatherData(
    val location: String,
    val current: CurrentWeather,
    val hourlyForecast: List<HourlyWeather>,
    val alerts: List<OpenWeatherAlert>?,  // Updated type
    val coordinates: Pair<Double, Double>
) {
    fun toWeatherAlerts(): List<WeatherAlert> {  // This returns the UI WeatherAlert
        val alerts = mutableListOf<WeatherAlert>()

        // Add current conditions alert
        val currentAlert = createCurrentWeatherAlert()
        alerts.add(currentAlert)

        // Add any weather alerts from API
        this.alerts?.forEach { apiAlert ->
            alerts.add(
                WeatherAlert(  // This is the UI WeatherAlert
                    location = location,
                    alertText = "${apiAlert.event}: ${apiAlert.description.take(100)}...",
                    alertLevel = AlertLevel.CRITICAL,
                    temperature = null,
                    weatherIcon = null
                )
            )
        }

        // Add next few hours forecast if conditions will worsen
        checkUpcomingConditions()?.let { alerts.add(it) }

        return alerts
    }

    private fun createCurrentWeatherAlert(): WeatherAlert {  // UI WeatherAlert
        val windInfo = buildString {
            append("Wind: ${String.format("%.1f", current.windSpeed)} m/s ")
            append("${getWindDirection(current.windDeg)} ")
            current.windGust?.let {
                append("(gusts ${String.format("%.1f", it)} m/s) ")
            }
        }

        val visibilityInfo = "Vis: ${current.visibility/1000}km"
        val waveInfo = "Humidity: ${current.humidity}%"

        val alertText = "$windInfo• $visibilityInfo • $waveInfo"

        return WeatherAlert(  // UI WeatherAlert
            location = "$location - Current",
            alertText = alertText,
            alertLevel = determineAlertLevel(current.windSpeed, current.windGust, current.visibility),
            temperature = current.temp.toInt(),
            weatherIcon = null
        )
    }

    private fun checkUpcomingConditions(): WeatherAlert? {  // UI WeatherAlert
        // Check next 6 hours for worsening conditions
        val next6Hours = hourlyForecast.take(6)
        val maxWind = next6Hours.maxByOrNull { it.windSpeed }
        val maxGust = next6Hours.mapNotNull { it.windGust }.maxOrNull()

        if (maxWind != null && maxWind.windSpeed > current.windSpeed * 1.5) {
            return WeatherAlert(  // UI WeatherAlert
                location = "$location - Next 6h",
                alertText = "Wind increasing to ${String.format("%.1f", maxWind.windSpeed)} m/s",
                alertLevel = AlertLevel.WARNING,
                temperature = null,
                weatherIcon = null
            )
        }

        return null
    }

    private fun determineAlertLevel(windSpeed: Double, windGust: Double?, visibility: Int): AlertLevel {
        return when {
            windSpeed > 10 || (windGust ?: 0.0) > 25 -> AlertLevel.CRITICAL
            windSpeed > 6 || visibility < 1000 -> AlertLevel.WARNING
            else -> AlertLevel.NORMAL
        }
    }

    private fun getWindDirection(degrees: Int): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((degrees + 22.5) / 45).toInt() % 8
        return directions[index]
    }
}