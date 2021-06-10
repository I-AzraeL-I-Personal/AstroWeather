package com.mycompany.astroweather.model

import com.mycompany.astroweather.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastService {

    @GET("data/2.5/onecall?exclude=minutely,hourly,alerts&units=metric&appid=$API_KEY")
    suspend fun getForecast(@Query("lat") latitude: Double, @Query("lon") longitude: Double): Response<Forecast>

    @GET("geo/1.0/direct?&limit=1&appid=${API_KEY}")
    suspend fun getGeocode(@Query("q") location: String): Response<List<Geocode>>
}