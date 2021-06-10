package com.mycompany.astroweather.fragment

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

class AdvancedInfoFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var windDirection: TextView
    private lateinit var windSpeed: TextView
    private lateinit var humidity: TextView
    private lateinit var visibility: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_advanced_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        windDirection = view.findViewById(R.id.windDirection)
        windSpeed = view.findViewById(R.id.windSpeed)
        humidity = view.findViewById(R.id.humidity)
        visibility = view.findViewById(R.id.visibility)
        viewModel.weatherData.observe(viewLifecycleOwner, { updateAdvancedInfo() })
    }

    private fun updateAdvancedInfo() {
        viewModel.weatherData.value?.apply {
            with (Util) {
                windDirection.text = ("${current.windDeg}°")
                windSpeed.text = formatSpeed(current.windSpeed, viewModel.currentUnit)
                humidity.text = ("${current.humidity}%")
                visibility.text = current.visibility.toString()
            }
        }
    }
}