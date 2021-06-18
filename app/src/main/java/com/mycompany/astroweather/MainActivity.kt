package com.mycompany.astroweather

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager.LayoutParams.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.mycompany.astroweather.activity.SettingsActivity
import com.mycompany.astroweather.fragment.*
import com.mycompany.astroweather.fragment.basic.*
import com.mycompany.astroweather.fragment.wrapper.InfoWrapperFragment
import com.mycompany.astroweather.fragment.wrapper.MoonSunWrapperFragment
import com.mycompany.astroweather.model.*
import com.mycompany.astroweather.model.data.Forecast
import com.mycompany.astroweather.model.service.ForecastManager
import com.mycompany.astroweather.model.service.ForecastService
import com.mycompany.astroweather.model.service.FragmentAdapter
import com.mycompany.astroweather.util.Secret
import com.mycompany.astroweather.util.Unit
import com.mycompany.astroweather.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.*


const val FORECAST_COUNT = 8
const val PRECISION = 4
const val EXPIRATION_MIN = 60

const val BASE_URL = "https://api.openweathermap.org/"
const val API_KEY = Secret.API_KEY

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var viewModel: MainViewModel
    private lateinit var forecastManager: ForecastManager
    private lateinit var file: File
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val forecastService = retrofit.create(ForecastService::class.java)
        forecastManager = ForecastManager(forecastService, applicationContext)

        val preferences = loadSharedPreferences()
        val delayMillis = preferences["delayMillis"] as Long
        val currentUnit = preferences["currentUnit"] as Unit

        file = File("${filesDir}/weather.json").also { it.createNewFile() }
        updateForecast()

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
            inflate(R.menu.location_menu, menu)
        }
        val spinner = (menu!!.findItem(R.id.spinner).actionView) as Spinner
        initCitySpinner(spinner)

        val editText = (menu.findItem(R.id.location).actionView) as EditText
        initCityEditText(editText)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        R.id.action_favorite -> {
            viewModel.deleteCurrentWeather()
            true
        }
        R.id.action_reload -> {
            updateForecast()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) super.onBackPressed() else viewPager.currentItem.minus(1)
    }

    private fun isOnline(): Boolean {
        val manager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return manager.isActiveNetworkMetered
    }

    private fun loadSharedPreferences(): Map<String, Any> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        with(sharedPreferences) {
            return mapOf("delayMillis" to getString("rate", "900000")!!.toLong(),
                "currentUnit" to Unit.values()[getString("unit", "0")!!.toInt()])
        }
    }
    private fun initViewPager() {
        viewPager = findViewById(R.id.pager)
        val fragments = mutableListOf(BasicInfoFragment::class, AdvancedInfoFragment::class,
            ForecastFragment::class, MoonFragment::class, SunFragment::class)
        val adapter = FragmentAdapter(supportFragmentManager, lifecycle, fragments)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            adapter.apply {
                remove(4, 3, 1, 0)
                add(mapOf(0 to InfoWrapperFragment::class, 2 to MoonSunWrapperFragment::class))
            }
        }
        viewPager.adapter = adapter
        (viewPager.getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

    private fun updateForecast() {
        if (isOnline()) {
            if (file.length() != 0L) {
                CoroutineScope(Dispatchers.IO).launch {
                    val forecastsToUpdate = loadDataFromFile(file).filter {
                        Util.minutesFromNow(it.current.time * 1000) > EXPIRATION_MIN }
                    val updatedForecasts = forecastManager.getUpdatedForecast(forecastsToUpdate)
                    if (updatedForecasts.isNotEmpty()) {
                        saveDataToFile(file, updatedForecasts)
                        withContext(Dispatchers.Main) {
                            viewModel.initViewModel()
                        }
                    }
                }
            }
        } else {
            showToast("Can't connect to the Internet. Some info may be outdated")
        }
    }

    private fun initCitySpinner(spinner: Spinner) {
        viewModel.weatherList.observe(this, { list ->
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                list.map { it.name })
            spinner.adapter = adapter
        })

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.setCurrentWeather(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initCityEditText(editText: EditText) {
        editText.apply {
            isSingleLine = true
            imeOptions = EditorInfo.IME_ACTION_DONE
            inputType = InputType.TYPE_CLASS_TEXT
            maxLines = 1
            window.setSoftInputMode(SOFT_INPUT_ADJUST_NOTHING or SOFT_INPUT_STATE_VISIBLE)
            hint = "Type a location"
        }
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                CoroutineScope(Dispatchers.IO).launch {
                    forecastManager.sendGeocodeRequest(editText.text.toString())?.let { geocode ->
                        if (viewModel.weatherList.value?.map { it.name }?.contains(geocode.name) == false) {
                            forecastManager.sendForecastRequest(geocode.latitude, geocode.longitude, geocode.name)?.let { forecast ->
                                withContext(Dispatchers.Main) {
                                    viewModel.addWeather(forecast)
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                showToast("City already saved")
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        editText.text.clear()
                        invalidateOptionsMenu()
                    }
                }
            }
            false
        }
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
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
}