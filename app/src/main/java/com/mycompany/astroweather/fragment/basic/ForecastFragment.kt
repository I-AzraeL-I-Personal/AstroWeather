package com.mycompany.astroweather.fragment.basic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.mycompany.astroweather.FORECAST_COUNT
import com.mycompany.astroweather.MainViewModel
import com.mycompany.astroweather.R
import com.mycompany.astroweather.util.Util

class ForecastFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var dayLabels: List<TextView>
    private lateinit var forecastDays: List<TextView>
    private lateinit var forecastImages: List<ImageView>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            val labels = mutableListOf<TextView>()
            val days = mutableListOf<TextView>()
            val images = mutableListOf<ImageView>()
            for (i in 0 until FORECAST_COUNT) {
                labels.add(findViewById(resources.getIdentifier("day${i + 1}Label", "id", context.packageName)))
                days.add(findViewById(resources.getIdentifier("day${i + 1}", "id", context.packageName)))
                images.add(findViewById(resources.getIdentifier("day${i + 1}img", "id", context.packageName)))
            }
            dayLabels = labels
            forecastDays = days
            forecastImages = images
        }
        viewModel.weatherData.observe(viewLifecycleOwner, { updateForecastInfo() })
    }

    private fun updateForecastInfo() {
        viewModel.weatherData.value?.apply {
            for (i in daily.indices) {
                val day = daily[i]
                with (Util) {
                    dayLabels[i].text = dateStringFromTimestamp(day.time, timezone)
                    forecastDays[i].text = (
                            "${formatTemperature(day.temperature.max, viewModel.currentUnit)} / " +
                                    formatTemperature(day.temperature.min, viewModel.currentUnit))
                }
                Glide.with(requireActivity())
                    .load("http://openweathermap.org/img/wn/${day.weather[0].icon}@2x.png")
                    .into(forecastImages[i])
            }
        }
    }
}