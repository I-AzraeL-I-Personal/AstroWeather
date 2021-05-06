package com.mycompany.astroweather

import com.astrocalculator.AstroDateTime
import java.math.RoundingMode

object Util {

    @JvmStatic
    fun format(number: Double): String = number.toBigDecimal()
        .setScale(PRECISION, RoundingMode.HALF_EVEN)
        .toPlainString()

    @JvmStatic
    fun formatDate(date: AstroDateTime): String = with(date) {
        String.format("%02d.%02d.%04d\n%02d:%02d", day, month, year, hour, minute) }
}