package com.mycompany.astroweather

import android.os.Bundle
import android.widget.TextClock
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

const val NUM_FRAGMENTS = 2
//todo set delay in settings menu
const val DELAY_MS = 3000L

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var mainClock: TextClock
    private lateinit var mainCoordinates: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel: MainViewModel by viewModels()

        mainClock = findViewById(R.id.mainTextClock)
        mainCoordinates = findViewById(R.id.location)
        mainCoordinates.text = viewModel.getFormattedCoordinates()
        initFragmentAdapter()
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) super.onBackPressed() else viewPager.currentItem -= 1
    }

    private fun initFragmentAdapter() {
        viewPager = findViewById(R.id.pager)
        viewPager.adapter = FragmentAdapter(this)
    }

    private class FragmentAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SunFragment()
                1 -> MoonFragment()
                else -> throw IllegalStateException("Unexpected value: $position")
            }
        }

        override fun getItemCount() = NUM_FRAGMENTS
    }
}