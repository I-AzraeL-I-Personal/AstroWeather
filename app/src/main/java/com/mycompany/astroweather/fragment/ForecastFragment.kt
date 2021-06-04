package com.mycompany.astroweather.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mycompany.astroweather.DAY_COUNT
import com.mycompany.astroweather.MainViewModel
import com.mycompany.astroweather.R
import java.util.*

class ForecastFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var dayLabels: List<TextView>
    private lateinit var forecastDays: List<TextView>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            val labels = mutableListOf<TextView>()
            val days = mutableListOf<TextView>()
            for (i in 1..DAY_COUNT) {
                labels.add(findViewById(resources.getIdentifier("day${i}Label", "id", context.packageName)))
                days.add(findViewById(resources.getIdentifier("day${i}", "id", context.packageName)))
            }
            dayLabels = labels
            forecastDays = days
        }
        updateForecastInfo()
    }

    private fun updateForecastInfo() {
        viewModel.weatherData.apply {
            for (i in 0 until DAY_COUNT) {
                val day = forecasts[i]
                val date = Calendar.getInstance().apply {
                    timeZone = TimeZone.getTimeZone(viewModel.weatherData.location.timezoneId)
                    timeInMillis = day.date * 1000
                }.let { String.format("%02d.%02d", it[Calendar.DAY_OF_MONTH], it[Calendar.MONTH] + 1) }
                dayLabels[i].text = ("${day.day} $date").toString()
                forecastDays[i].text = ("High: ${day.high}\nLow: ${day.low}\n${day.text}").toString()
            }
        }
    }
}