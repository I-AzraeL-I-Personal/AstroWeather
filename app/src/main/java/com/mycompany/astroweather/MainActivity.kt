package com.mycompany.astroweather

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextClock
import android.widget.TextView
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
import com.mycompany.astroweather.model.Weather
import com.mycompany.astroweather.util.Util

const val FRAGMENT_COUNT = 5
const val DAY_COUNT = 10
const val PRECISION = 4

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var mainClock: TextClock
    private lateinit var mainCoordinates: TextView
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainClock = findViewById(R.id.mainTextClock)
        mainCoordinates = findViewById(R.id.location)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        viewModel.location.value?.let {
            sharedPreferences?.apply {
                it.latitude = getString("latitude", getString(R.string.latitude_default_value))!!.toDouble()
                it.longitude = getString("longitude", getString(R.string.longitude_default_value))!!.toDouble()
                viewModel.delayMillis = getString("rate", "900000")!!.toLong()
            }
        }

        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
        val jsonString = applicationContext.assets.open("weather.json").bufferedReader().readText()
        val model = gson.fromJson(jsonString, Weather::class.java)
        viewModel.weatherData = model

        //viewModel.location.observe(this, { updateCoordinates() })
        updateCoordinates()

        viewPager = findViewById<ViewPager2>(R.id.pager).apply {
            adapter = FragmentAdapter(supportFragmentManager, lifecycle)
            (getChildAt(0) as RecyclerView).apply {
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setPadding(0, 0,
                        Resources.getSystem().displayMetrics.widthPixels / 2, 0)
                    clipToPadding = false
                    isUserInputEnabled = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewPager.currentItem = 0
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) super.onBackPressed() else viewPager.currentItem -= 1
    }

    private fun updateCoordinates() {
        mainCoordinates.text = viewModel.location.value?.run {
            Util.formatCords(latitude, longitude)
        }
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