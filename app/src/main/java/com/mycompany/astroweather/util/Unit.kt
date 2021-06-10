package com.mycompany.astroweather.util

enum class Unit(val speed: String, val temperature: String, val speedConversion: Double, val temperatureConversion: Int) {
    METRIC("m/s", "°C", 0.447, -32),
    IMPERIAL("mph", "°F", 2.2371, 32),
}