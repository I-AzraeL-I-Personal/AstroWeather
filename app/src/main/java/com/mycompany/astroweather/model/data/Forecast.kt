package com.mycompany.astroweather.model.data

import com.google.gson.annotations.SerializedName

data class Forecast(
    var name: String,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    val timezone: String,
    val timezoneOffset: Long,
    val current: Current,
    val daily: List<Daily>
)

data class Current(
    @SerializedName("dt") val time: Long,
    val sunrise: Long,
    val sunset: Long,
    @SerializedName("temp") val temperature: Double,
    val feelsLike: Double,
    val pressure: Int,
    val humidity: Int,
    val dewPoint: Double,
    val uvi: Double,
    val clouds: Int,
    val visibility: Long,
    val windSpeed: Double,
    val windDeg: Int,
    val weather: List<Weather>
)

data class Daily(
    @SerializedName("dt") val time: Long,
    val sunrise: Long,
    val sunset: Long,
    val moonrise: Long,
    val moonset: Long,
    val moonPhase: Double,
    @SerializedName("temp") val temperature: Temperature,
    val feelsLike: FeelsLike,
    val pressure: Int,
    val humidity: Int,
    val dewPoint: Double,
    val windSpeed: Double,
    val windDeg: Int,
    val windGust: Double,
    val weather: List<Weather>,
    val clouds: Int,
    val pop: Double,
    val rain: Double,
    val uvi: Double
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Temperature(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    @SerializedName("eve") val evening: Double,
    @SerializedName("morn") val morning: Double
)

data class FeelsLike(
    val day: Double,
    val night: Double,
    @SerializedName("eve") val evening: Double,
    @SerializedName("morn") val morning: Double
)


