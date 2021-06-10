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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val forecastService = retrofit.create(ForecastService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = loadSharedPreferences()
        val delayMillis = preferences["delayMillis"] as Long
        val currentUnit = preferences["currentUnit"] as Unit

        val file = File("${filesDir}/weather.json")
        if (isOnline()) {
            val forecasts = loadDataFromFile(file)
            val updatedForecasts = updateForecast(forecasts)
            saveDataToFile(file, updatedForecasts)
        } else {
            Toast.makeText(applicationContext,
                "Can't connect to the Internet. Some info may be outdated",
                Toast.LENGTH_LONG).show()
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

        val searchView = (menu.findItem(R.id.location).actionView) as EditText
        initCityEditText(searchView)

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
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            viewModel.weatherList.map { forecast -> forecast.name }).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter
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
                sendGeocodeRequestAsync(editText.text.toString())
                editText.text.clear()
            }
            false
        }
    }

    private fun sendGeocodeRequestAsync(location: String) {
        forecastService.getGeocode(location).enqueue(object : Callback<List<Geocode>> {
            override fun onResponse(call: Call<List<Geocode>>, response: Response<List<Geocode>>) {
                if (response.isSuccessful) {
                    val geocode = response.body()?.get(0)
                    if (geocode != null) {
                        sendForecastRequestAsync(geocode.latitude, geocode.longitude, geocode.name)
                    }
                } else {
                    Toast.makeText(applicationContext, "Not found", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<Geocode>>, t: Throwable) {
                Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun sendForecastRequestAsync(latitude: Double, longitude: Double, cityName: String) {
        forecastService.getForecast(latitude, longitude).enqueue(object : Callback<Forecast> {
            override fun onResponse(call: Call<Forecast>, response: Response<Forecast>) {
                if (response.isSuccessful) {
                    val forecast = response.body()?.apply { name = cityName }
                    if (forecast != null) {
                        viewModel.addWeather(forecast)
                        invalidateOptionsMenu()
                    }
                } else {
                    Toast.makeText(applicationContext, "Not found", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<Forecast>, t: Throwable) {
                Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun sendForecastRequest(latitude: Double, longitude: Double, cityName: String): Forecast? {
        return forecastService.getForecast(latitude, longitude).execute().body()?.apply { name = cityName }
    }

    private fun updateForecast(forecast: List<Forecast>): List<Forecast> {
        val updatedForecast = mutableListOf<Forecast>()
        forecast.forEach {
            if (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - it.current.time * 1000) > 1) {
                CoroutineScope(Dispatchers.IO).launch {
                    sendForecastRequest(it.latitude, it.longitude, it.name)?.let { item ->
                        updatedForecast.add(item)
                    }
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