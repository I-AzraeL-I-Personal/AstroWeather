package com.mycompany.astroweather.fragment.wrapper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextClock
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.mycompany.astroweather.MainViewModel
import com.mycompany.astroweather.R
import com.mycompany.astroweather.util.Util

class InfoWrapperFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var city: TextView
    private lateinit var coordinates: TextView
    private lateinit var time: TextClock
    private lateinit var temperature: TextView
    private lateinit var pressure: TextView
    private lateinit var conditions: TextView
    private lateinit var conditionsImage: ImageView
    private lateinit var windDirection: TextView
    private lateinit var windSpeed: TextView
    private lateinit var humidity: TextView
    private lateinit var visibility: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info_wrapper, container, false)
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
            conditionsImage = findViewById(R.id.conditionsImg)
            windDirection = view.findViewById(R.id.windDirection)
            windSpeed = view.findViewById(R.id.windSpeed)
            humidity = view.findViewById(R.id.humidity)
        }
        this.visibility = view.findViewById(R.id.visibility)
        viewModel.weatherData.observe(viewLifecycleOwner, { updateInfo() })
    }

    private fun updateInfo() {
        viewModel.weatherData.value?.apply {
            with (Util) {
                city.text = name
                coordinates.text = formatCords(latitude, longitude)
                time.timeZone = timezone
                temperature.text = formatTemperature(current.temperature, viewModel.currentUnit)
                pressure.text = ("${current.pressure} hPa")
                conditions.text = current.weather[0].description
                windDirection.text = ("${current.windDeg}°")
                windSpeed.text = formatSpeed(current.windSpeed, viewModel.currentUnit)
                humidity.text = ("${current.humidity}%")
                visibility.text = current.visibility.toString()
            }
            Glide.with(requireActivity())
                .load("http://openweathermap.org/img/wn/${current.weather[0].icon}@2x.png")
                .into(conditionsImage)
        }
    }
}