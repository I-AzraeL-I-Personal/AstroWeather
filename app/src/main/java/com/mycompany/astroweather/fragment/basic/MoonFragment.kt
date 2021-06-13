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
import kotlin.math.abs
import kotlin.math.roundToInt

class MoonFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var moonriseTime: TextView
    private lateinit var moonsetTime: TextView
    private lateinit var newMoonTime: TextView
    private lateinit var fullMoonTime: TextView
    private lateinit var moonPhase: TextView
    private lateinit var lunarDay: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_moon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            moonriseTime = findViewById(R.id.moonriseTime)
            moonsetTime = findViewById(R.id.moonsetTime)
            newMoonTime = findViewById(R.id.newMoonTime)
            fullMoonTime = findViewById(R.id.fullMoonTime)
            moonPhase = findViewById(R.id.moonPhase)
            lunarDay = findViewById(R.id.lunarDay)
        }
        viewModel.astroCalculator.observe(viewLifecycleOwner, { updateMoonInfo() })
    }

    private fun updateMoonInfo() {
        viewModel.astroCalculator.value?.moonInfo?.apply {
            with(Util) {
                moonriseTime.text = formatDate(moonrise)
                moonsetTime.text = formatDate(moonset)
                newMoonTime.text = formatDate(nextNewMoon)
                fullMoonTime.text = formatDate(nextFullMoon)
                moonPhase.text = ("${illumination.times(100).roundToInt()}%")
                lunarDay.text = format(abs(age) / 1.2)
            }
        }
    }
}