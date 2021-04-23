package com.mycompany.astroweather

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class SunFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var sunriseAzimuth: TextView
    private lateinit var sunsetAzimuth: TextView
    private lateinit var sunriseTime: TextView
    private lateinit var sunsetTime: TextView
    private lateinit var twilightTime: TextView
    private lateinit var civilDawnTime: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sun, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sunriseAzimuth = view.findViewById(R.id.sunriseAzimuth)
        sunsetAzimuth = view.findViewById(R.id.sunsetAzimuth)
        sunriseTime = view.findViewById(R.id.sunriseTime)
        sunsetTime = view.findViewById(R.id.sunsetTime)
        twilightTime = view.findViewById(R.id.twilightTime)
        civilDawnTime = view.findViewById(R.id.civilDawnTime)
        viewModel.astroCalculator.observe(viewLifecycleOwner, { updateSunInfo() })
    }

    private fun updateSunInfo() {
        viewModel.astroCalculator.value?.sunInfo?.let {
            sunriseAzimuth.text = it.azimuthRise.toString()
            sunsetAzimuth.text = it.azimuthSet.toString()
            sunriseTime.text = it.sunrise.toString()
            sunsetTime.text = it.sunset.toString()
            twilightTime.text = it.twilightEvening.toString()
            civilDawnTime.text = it.twilightMorning.toString()
        }
    }
}