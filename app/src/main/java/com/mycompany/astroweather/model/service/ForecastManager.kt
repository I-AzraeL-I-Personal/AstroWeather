package com.mycompany.astroweather.model.service

import android.content.Context
import android.widget.Toast
import com.mycompany.astroweather.model.data.Forecast
import com.mycompany.astroweather.model.data.Geocode

class ForecastManager(private val forecastService: ForecastService, private val context: Context) {

    suspend fun sendGeocodeRequest(location: String): Geocode? {
        val response = forecastService.getGeocode(location)
        var geocode: Geocode? = null
        when {
            response.isSuccessful -> geocode = response.body()?.get(0)
            response.code() == 404 -> showToast("City not found")
            else -> showToast("Error")
        }
        return geocode
    }

    suspend fun sendForecastRequest(latitude: Double, longitude: Double, cityName: String): Forecast? {
        val response = forecastService.getForecast(latitude, longitude)
        var forecast: Forecast? = null
        when {
            response.isSuccessful -> forecast = response.body()?.apply { name = cityName }
            else -> showToast("Error")
        }
        return forecast
    }

    suspend fun getUpdatedForecast(forecast: List<Forecast>): List<Forecast> {
        return forecast.mapNotNull {
            sendForecastRequest(it.latitude, it.longitude, it.name)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}