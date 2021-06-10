package com.mycompany.astroweather

import androidx.lifecycle.*
import com.astrocalculator.AstroCalculator
import com.astrocalculator.AstroDateTime
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.mycompany.astroweather.model.Forecast
import com.mycompany.astroweather.util.Unit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class MainViewModel(private val delayMillis: Long, private val file: File, val currentUnit: Unit) : ViewModel() {

    private val _location = MutableLiveData(AstroCalculator.Location(0.0, 0.0))
    private val _astroCalculator = MutableLiveData(AstroCalculator(createAstroDateTime(), _location.value))
    private val _weatherData = MutableLiveData<Forecast>()
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    val weatherList: MutableList<Forecast>
    val astroCalculator: LiveData<AstroCalculator> = _astroCalculator
    val weatherData: LiveData<Forecast>  = _weatherData

    init {
        weatherList = loadDataFromFile()
        viewModelScope.launch {
            while (true) {
                _astroCalculator.value = astroCalculator.value?.apply { dateTime = createAstroDateTime() }
                delay(delayMillis)
            }
        }
    }

    fun setCurrentWeather(id: Int) {
        _weatherData.value = weatherList[id]
        _location.value?.apply {
            latitude = weatherList[id].latitude
            longitude = weatherList[id].longitude
        }
        _astroCalculator.value = astroCalculator.value?.apply { dateTime = createAstroDateTime() }
    }

    fun addWeather(weather: Forecast) {
        weatherList.add(weather)
        file.writeText(gson.toJson(weatherList))
    }

    fun clearWeather() {
        weatherList.clear()
        file.writeText(gson.toJson(weatherList))
    }

    private fun loadDataFromFile(): MutableList<Forecast> {
        return gson.fromJson(file.readText(), Array<Forecast>::class.java).toMutableList()
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

class MainViewModelFactory(private val delayMillis: Long, private val file: File, private val currentUnit: Unit)
    : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(delayMillis, file, currentUnit) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}