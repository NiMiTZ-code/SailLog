package com.niemi.saillog.ui.screens

import android.app.Application
import android.location.Geocoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.niemi.saillog.data.SailingWeatherData
import com.niemi.saillog.data.WeatherAlert
import com.niemi.saillog.location.LocationManager
import com.niemi.saillog.network.ApiConfig
import com.niemi.saillog.network.WeatherApiService
import kotlinx.coroutines.launch
import java.util.Locale

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val weatherApi = WeatherApiService.create()
    private val locationManager = LocationManager(application)
    private val geocoder = Geocoder(application, Locale.getDefault())

    var weatherAlerts by mutableStateOf<List<WeatherAlert>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var lastUpdateTime by mutableStateOf<Long?>(null)
        private set

    fun loadWeatherData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val location = locationManager.getCurrentLocation()

                if (location != null) {
                    // Get location name from coordinates
                    val addresses = try {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    } catch (e: Exception) {
                        null
                    }

                    val locationName = addresses?.firstOrNull()?.let { address ->
                        address.locality ?: address.subAdminArea ?: "Unknown Location"
                    } ?: "Lat: ${String.format("%.2f", location.latitude)}, Lon: ${String.format("%.2f", location.longitude)}"

                    // Fetch weather data - excluding minutely data to reduce response size
                    val weatherResponse = weatherApi.getOneCallWeather(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        exclude = "minutely",
                        apiKey = ApiConfig.OPENWEATHER_API_KEY
                    )

                    val sailingData = SailingWeatherData(
                        location = locationName,
                        current = weatherResponse.current,
                        hourlyForecast = weatherResponse.hourly ?: emptyList(),
                        alerts = weatherResponse.alerts,
                        coordinates = location.latitude to location.longitude
                    )

                    weatherAlerts = sailingData.toWeatherAlerts()
                    lastUpdateTime = System.currentTimeMillis()
                } else {
                    errorMessage = "Unable to get location. Please enable location services."
                }
            } catch (e: Exception) {
                errorMessage = when {
                    e.message?.contains("401") == true -> "Invalid API key. Please check your OpenWeather API key."
                    e.message?.contains("404") == true -> "Weather data not available for this location."
                    else -> "Error loading weather: ${e.message}"
                }
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}