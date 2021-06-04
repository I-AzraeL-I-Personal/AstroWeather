package com.mycompany.astroweather.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Weather(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @Embedded var location: Location,
    @Embedded var currentObservation: CurrentObservation,
    @Embedded var forecasts: List<Forecast> = listOf()
)

data class Location(
    var woeid: Long = 0,
    var city: String = "",
    var region: String = "",
    var country: String = "",
    var lat: Double = 0.0,
    var long: Double = 0.0,
    var timezoneId: String = ""
)

data class CurrentObservation(
    @Embedded var wind: Wind,
    @Embedded var atmosphere: Atmosphere,
    @Embedded var astronomy: Astronomy,
    @Embedded var condition: Condition,
    @SerializedName("pubDate") var pubDate: Long = 0
)

data class Forecast(
    var day: String = "",
    var date: Long = 0,
    var low: Int = 0,
    var high: Int = 0,
    var text: String = "",
    var code: Int = 0
)


data class Wind(
    var chill: Int = 0,
    var direction: Int = 0,
    var speed: Double = 0.0
)

data class Atmosphere(
    var humidity: Int = 0,
    var visibility: Int = 0,
    var pressure: Double = 0.0
)

data class Astronomy(
    var sunrise: String = "",
    var sunset: String = ""
)

data class Condition(
    var text: String = "",
    var code: Int = 0,
    var temperature: Int = 0
)


