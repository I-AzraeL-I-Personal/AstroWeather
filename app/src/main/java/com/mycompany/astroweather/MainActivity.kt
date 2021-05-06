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

const val FRAGMENT_COUNT = 2
const val PRECISION = 4

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var mainClock: TextClock
    private lateinit var mainCoordinates: TextView
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        mainClock = findViewById(R.id.mainTextClock)
        mainCoordinates = findViewById(R.id.location)

        viewModel.location.observe(this, { updateCoordinates() })

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        viewModel.location.value?.let {
            sharedPreferences?.apply {
                it.latitude = getString("latitude", getString(R.string.latitude_default_value))!!.toDouble()
                it.longitude = getString("longitude", getString(R.string.longitude_default_value))!!.toDouble()
                viewModel.delayMillis = getString("rate", "900000")!!.toLong()
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
            with(Util) {
                "${format(latitude)} ${format(longitude)}"
            }
        }
    }

    private class FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle)
        : FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SunFragment()
                1 -> MoonFragment()
                else -> throw IllegalStateException("Unexpected value: $position")
            }
        }
        override fun getItemCount() = FRAGMENT_COUNT
    }
}