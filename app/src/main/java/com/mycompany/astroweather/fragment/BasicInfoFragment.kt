package com.mycompany.astroweather.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextClock
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mycompany.astroweather.MainViewModel
import com.mycompany.astroweather.R
import com.mycompany.astroweather.util.Util

class BasicInfoFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var city: TextView
    private lateinit var coordinates: TextView
    private lateinit var time: TextClock
    private lateinit var temperature: TextView
    private lateinit var pressure: TextView
    private lateinit var conditions: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_basic_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            city = findViewById(R.id.city)
            coordinates = findViewById(R.id.coordinates)
            time = findViewById(R.id.time)
            temperature = findViewById(R.id.temerature)
            pressure = findViewById(R.id.pressure)
            conditions = findViewById(R.id.conditions)
        }
        viewModel.weatherData.observe(viewLifecycleOwner, { updateBasicInfo() })
    }

    private fun updateBasicInfo() {
        viewModel.weatherData.value?.apply {
            with (Util) {
                city.text = name
                coordinates.text = formatCords(latitude, longitude)
                time.timeZone = timezone
                temperature.text = formatTemperature(current.temperature, viewModel.currentUnit)
                pressure.text = ("${current.pressure} hPa")
                conditions.text = current.weather[0].description
            }
        }
    }
}