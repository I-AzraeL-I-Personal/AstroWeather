package com.mycompany.astroweather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astrocalculator.AstroCalculator
import com.astrocalculator.AstroDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

class MainViewModel : ViewModel() {

    //todo set cords in settings menu
    private val location = AstroCalculator.Location("51.7833".toDouble(), "19.4667".toDouble())
    val astroCalculator = MutableLiveData(AstroCalculator(getCurrentAstroDateTime(), location))

    init {
        viewModelScope.launch {
            while (true) {
                astroCalculator.value?.dateTime = getCurrentAstroDateTime()
                astroCalculator.value = astroCalculator.value
                delay(DELAY_MS)
            }
        }
    }

    private fun updateAstroCaculator() {
        astroCalculator.let {
            it.value?.dateTime = getCurrentAstroDateTime()
            it.value?.location = location
        }
    }

    fun getFormattedCoordinates(): String {
        val latitude = location.latitude.absoluteValue.toString() + when {
            location.latitude > 0 -> "N"
            location.latitude < 0 -> "S"
            else -> ""
        }
        val longitude = location.longitude.absoluteValue.toString() + when {
            location.longitude > 0 -> "E"
            location.longitude < 0 -> "W"
            else -> ""
        }
        return "$latitude $longitude"
    }

    private fun getCurrentAstroDateTime(): AstroDateTime {
        return Calendar.getInstance().let {
            AstroDateTime(
                it[Calendar.YEAR],
                it[Calendar.MONTH] + 1,
                it[Calendar.DAY_OF_MONTH],
                it[Calendar.HOUR_OF_DAY],
                it[Calendar.MINUTE],
                it[Calendar.SECOND],
                TimeUnit.MILLISECONDS.toHours(it[Calendar.ZONE_OFFSET].toLong()).toInt(),
                it[Calendar.DST_OFFSET] != 0)
        }
    }
}