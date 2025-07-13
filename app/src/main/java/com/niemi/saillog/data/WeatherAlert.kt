package com.niemi.saillog.data

data class WeatherAlert(
    val location: String,
    val alertText: String,
    val alertLevel: AlertLevel,
    val temperature: Int? = null,
    val weatherIcon: Int? = null
)

enum class AlertLevel {
    NORMAL,
    WARNING,
    CRITICAL
}