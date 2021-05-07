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

class MainViewModel : ViewModel() {

    val location = MutableLiveData(AstroCalculator.Location("0".toDouble(), "0".toDouble()))
    val astroCalculator = MutableLiveData(AstroCalculator(createAstroDateTime(), location.value))
    var delayMillis = 1000L

    init {
        viewModelScope.launch {
            while (true) {
                astroCalculator.value = astroCalculator.value?.apply { dateTime = createAstroDateTime() }
                delay(delayMillis)
            }
        }
    }

    private fun createAstroDateTime(): AstroDateTime {
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