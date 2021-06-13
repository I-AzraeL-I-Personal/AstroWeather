package com.mycompany.astroweather

import androidx.lifecycle.*
import com.astrocalculator.AstroCalculator
import com.astrocalculator.AstroDateTime
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.mycompany.astroweather.model.data.Forecast
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
    private val _weatherList = MutableLiveData<MutableList<Forecast>>(mutableListOf())
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    val weatherList: LiveData<MutableList<Forecast>> = _weatherList
    val astroCalculator: LiveData<AstroCalculator> = _astroCalculator
    val weatherData: LiveData<Forecast>  = _weatherData

    init {
        if (file.length() != 0L) {
            _weatherList.value = loadDataFromFile()
        }
        viewModelScope.launch {
            while (true) {
                _astroCalculator.value = _astroCalculator.value?.apply { dateTime = createAstroDateTime() }
                delay(delayMillis)
            }
        }
    }

    fun setCurrentWeather(id: Int) {
        _weatherData.value = _weatherList.value?.get(id)
        _location.value?.apply {
            latitude = _weatherData.value!!.latitude
            longitude = _weatherData.value!!.longitude
        }
        _astroCalculator.value = astroCalculator.value?.apply { dateTime = createAstroDateTime() }
    }

    fun addWeather(weather: Forecast) {
        _weatherList.value = _weatherList.value?.apply { add(weather) }
        saveDataToFile()
    }

    fun deleteCurrentWeather() {
        if (_weatherList.value?.size!! > 1) {
            _weatherList.value = _weatherList.value.apply { _weatherData.value?.let { this?.remove(it) } }
            setCurrentWeather(0)
            saveDataToFile()
        }
    }

    private fun loadDataFromFile(): MutableList<Forecast> {
        return gson.fromJson(file.readText(), Array<Forecast>::class.java).toMutableList()
    }

    private fun saveDataToFile() {
        file.writeText(gson.toJson(weatherList.value))
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