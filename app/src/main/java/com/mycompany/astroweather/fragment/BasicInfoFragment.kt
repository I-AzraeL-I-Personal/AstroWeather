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

class BasicInfoFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var city: TextView
    private lateinit var coordinates: TextView
    private lateinit var time: TextView
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
        updateBasicInfo()
    }

    private fun updateBasicInfo() {
        viewModel.weatherData.apply {
            with (Util) {
                city.text = location.city
                coordinates.text = formatCords(location.lat, location.long)
                time.text = location.timezoneId
                temperature.text = currentObservation.condition.temperature.toString()
                pressure.text = format(currentObservation.atmosphere.pressure)
                conditions.text = currentObservation.condition.text
            }
        }
    }
}