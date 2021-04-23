package com.mycompany.astroweather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
        moonriseTime = view.findViewById(R.id.moonriseTime)
        moonsetTime = view.findViewById(R.id.moonsetTime)
        newMoonTime = view.findViewById(R.id.newMoonTime)
        fullMoonTime = view.findViewById(R.id.fullMoonTime)
        moonPhase = view.findViewById(R.id.moonPhase)
        lunarDay = view.findViewById(R.id.lunarDay)
        viewModel.astroCalculator.observe(viewLifecycleOwner, { updateMoonInfo() })
    }

    private fun updateMoonInfo() {
        viewModel.astroCalculator.value?.moonInfo?.let {
            moonriseTime.text = it.moonrise.toString()
            moonsetTime.text = it.moonset.toString()
            newMoonTime.text = it.nextNewMoon.toString()
            fullMoonTime.text = it.nextFullMoon.toString()
            moonPhase.text = ("${it.illumination.times(100).roundToInt()}%")
            lunarDay.text = it.age.toString()
        }
    }
}