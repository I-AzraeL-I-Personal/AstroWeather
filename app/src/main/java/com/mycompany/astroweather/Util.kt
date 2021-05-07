package com.mycompany.astroweather

import com.astrocalculator.AstroDateTime
import java.math.BigDecimal
import java.math.RoundingMode

object Util {

    @JvmStatic
    fun format(number: Double): String {
        if (number.isNaN()) {
            return "0"
        }
        return BigDecimal(number)
            .setScale(PRECISION, RoundingMode.HALF_EVEN)
            .toPlainString()
    }

    @JvmStatic
    fun formatDate(date: AstroDateTime): String = with(date) {
        String.format("%02d.%02d.%04d\n%02d:%02d", day, month, year, hour, minute) }
}