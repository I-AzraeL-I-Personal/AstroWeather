package com.mycompany.astroweather.fragment.basic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mycompany.astroweather.MainViewModel
import com.mycompany.astroweather.R
import com.mycompany.astroweather.util.Util

class SunFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var sunriseAzimuth: TextView
    private lateinit var sunsetAzimuth: TextView
    private lateinit var sunriseTime: TextView
    private lateinit var sunsetTime: TextView
    private lateinit var civilTwilightTime: TextView
    private lateinit var civilDawnTime: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sun, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            sunriseAzimuth = findViewById(R.id.sunriseAzimuth)
            sunsetAzimuth = findViewById(R.id.sunsetAzimuth)
            sunriseTime = findViewById(R.id.sunriseTime)
            sunsetTime = findViewById(R.id.sunsetTime)
            civilTwilightTime = findViewById(R.id.civilTwilightTime)
            civilDawnTime = findViewById(R.id.civilDawnTime)
        }
        viewModel.astroCalculator.observe(viewLifecycleOwner, { updateSunInfo() })
    }

    private fun updateSunInfo() {
        viewModel.astroCalculator.value?.sunInfo?.apply {
            with(Util) {
                sunriseAzimuth.text = format(azimuthRise)
                sunsetAzimuth.text = format(azimuthSet)
                sunriseTime.text = formatDate(sunrise)
                sunsetTime.text = formatDate(sunset)
                civilTwilightTime.text = formatDate(twilightEvening)
                civilDawnTime.text = formatDate(twilightMorning)
            }
        }
    }
}