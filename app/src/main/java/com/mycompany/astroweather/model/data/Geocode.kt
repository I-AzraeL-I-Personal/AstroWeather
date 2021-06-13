package com.mycompany.astroweather.model.data

import com.google.gson.annotations.SerializedName

data class Geocode(
    val name: String,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    val country: String,
    var state: String = ""
)
