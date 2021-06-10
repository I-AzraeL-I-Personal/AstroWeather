package com.mycompany.astroweather

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.mycompany.astroweather.activity.SettingsActivity
import com.mycompany.astroweather.fragment.*
import com.mycompany.astroweather.model.Forecast
import com.mycompany.astroweather.model.ForecastService
import com.mycompany.astroweather.model.Geocode
import com.mycompany.astroweather.util.Secret
import com.mycompany.astroweather.util.Unit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


const val FRAGMENT_COUNT = 5
const val FORECAST_COUNT = 8
const val PRECISION = 4

const val BASE_URL = "https://api.openweathermap.org/"
const val API_KEY = Secret.API_KEY

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var viewModel: MainViewModel
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    private val forecastService = retrofit.create(ForecastService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = loadSharedPreferences()
        val delayMillis = preferences["delayMillis"] as Long
        val currentUnit = preferences["currentUnit"] as Unit

        val file = File("${filesDir}/weather.json").also { it.createNewFile() }
        if (isOnline()) {
            if (file.length() != 0L) {
                CoroutineScope(Dispatchers.IO).launch {
                    val forecasts = loadDataFromFile(file)
                    val updatedForecasts = updateForecast(forecasts)
                    saveDataToFile(file, updatedForecasts)
                }
            }
        } else {
            showToast("Can't connect to the Internet. Some info may be outdated")
        }

        val model: MainViewModel by viewModels {
            MainViewModelFactory(delayMillis, file, currentUnit) }
        viewModel = model
        initViewPager()
    }

    override fun onResume() {
        super.onResume()
        viewPager.currentItem = 0
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        with(menuInflater) {
            inflate(R.menu.settings_menu, menu)
            inflate(R.menu.location_menu, menu)
        }
        val spinner = (menu!!.findItem(R.id.spinner).actionView) as Spinner
        initCitySpinner(spinner)

        val editText = (menu.findItem(R.id.location).actionView) as EditText
        initCityEditText(editText)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return false
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) super.onBackPressed() else viewPager.currentItem -= 1
    }

    private fun isOnline(): Boolean {
        val manager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetworkInfo
        return network != null && network.isConnectedOrConnecting()
    }

    private fun loadSharedPreferences(): Map<String, Any> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        with(sharedPreferences) {
            return mapOf("delayMillis" to getString("rate", "900000")!!.toLong(),
                "currentUnit" to Unit.values()[getString("unit", "0")!!.toInt()])
        }
    }

    private fun initViewPager() {
        viewPager = findViewById<ViewPager2>(R.id.pager).apply {
            adapter = FragmentAdapter(supportFragmentManager, lifecycle)
            (getChildAt(0) as RecyclerView).apply {
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            }
        }
    }

    private fun initCitySpinner(spinner: Spinner) {
        viewModel.weatherList.observe(this, {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item,
                it.map { forecast -> forecast.name })
            spinner.adapter = adapter
        })

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) = viewModel.setCurrentWeather(position)
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initCityEditText(editText: EditText) {
        editText.apply {
            isSingleLine = true
            imeOptions = EditorInfo.IME_ACTION_DONE
            inputType = InputType.TYPE_CLASS_TEXT
            maxLines = 1
        }
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                CoroutineScope(Dispatchers.IO).launch {
                    sendGeocodeRequest(editText.text.toString())?.let { geocode ->
                        sendForecastRequest(geocode.latitude, geocode.longitude, geocode.name)?.let { forecast ->
                            withContext(Dispatchers.Main) {
                                viewModel.addWeather(forecast)
                                editText.text.clear()
                                invalidateOptionsMenu()
                            }
                        }
                    }
                }
            }
            false
        }
    }

    private suspend fun sendGeocodeRequest(location: String): Geocode? {
        val response = forecastService.getGeocode(location)
        var geocode: Geocode? = null
        when {
            response.isSuccessful -> geocode = response.body()?.get(0)
            response.code() == 404 -> showToast("City not found")
            else -> showToast("Error")
        }
        return geocode
    }

    private suspend fun sendForecastRequest(latitude: Double, longitude: Double, cityName: String): Forecast? {
        val response = forecastService.getForecast(latitude, longitude)
        var forecast: Forecast? = null
        when {
            response.isSuccessful -> forecast = response.body()?.apply { name = cityName }
            else -> Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
        }
        return forecast
    }

    private suspend fun updateForecast(forecast: List<Forecast>): List<Forecast> {
        val updatedForecast = mutableListOf<Forecast>()
        forecast.forEach {
            if (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - it.current.time * 1000) > 1) {
                sendForecastRequest(it.latitude, it.longitude, it.name)?.let { item ->
                    updatedForecast.add(item)
                }
            } else {
                updatedForecast.add(it)
            }
        }
        return updatedForecast
    }

    private fun loadDataFromFile(file: File): MutableList<Forecast> {
        return gson.fromJson(file.readText(), Array<Forecast>::class.java).toMutableList()
    }

    private fun saveDataToFile(file: File, forecast: List<Forecast>) {
        file.writeText(gson.toJson(forecast))
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private class FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle)
        : FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> BasicInfoFragment()
                1 -> AdvancedInfoFragment()
                2 -> ForecastFragment()
                3 -> SunFragment()
                4 -> MoonFragment()
                else -> throw IllegalStateException("Unexpected value: $position")
            }
        }
        override fun getItemCount() = FRAGMENT_COUNT
    }
}