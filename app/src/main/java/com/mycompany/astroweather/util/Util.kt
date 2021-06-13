package com.mycompany.astroweather.util

import com.astrocalculator.AstroDateTime
import com.mycompany.astroweather.PRECISION
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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
    fun format(number: Double, precision: Int): String {
        if (number.isNaN()) {
            return "0"
        }
        return BigDecimal(number)
            .setScale(precision, RoundingMode.HALF_EVEN)
            .toPlainString()
    }

    @JvmStatic
    fun formatCords(latitude: Double, longitude: Double): String {
        return "${format(latitude)} ${format(longitude)}"
    }

    @JvmStatic
    fun formatDate(date: AstroDateTime): String = with(date) {
        String.format("%02d.%02d.%04d\n%02d:%02d", day, month, year, hour, minute) }

    @JvmStatic
    fun dateFromTimestamp(time: Long, timezone: String): Calendar {
        return Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone(timezone)
            timeInMillis = time * 1000
        }
    }

    @JvmStatic
    fun dateStringFromTimestamp(time: Long, timezone: String): String {
        return dateFromTimestamp(time, timezone).let {
            String.format("%02d.%02d", it[Calendar.DAY_OF_MONTH], it[Calendar.MONTH] + 1) }
    }

    @JvmStatic
    fun formatSpeed(number: Double, toUnit: Unit): String {
        return if (toUnit != Unit.METRIC) {
            "${format(number * toUnit.speedConversion, 1)} ${toUnit.speed}"
        } else {
            "${format(number, 1)} ${toUnit.speed}"
        }
    }

    @JvmStatic
    fun formatTemperature(number: Double, toUnit: Unit): String {
        return if (toUnit != Unit.METRIC) {
            "${format(number + toUnit.temperatureConversion, 1)}${toUnit.temperature}"
        } else {
            "${format(number, 1)}${toUnit.temperature}"
        }
    }

    @JvmStatic
    fun minutesFromNow(timestamp: Long): Long {
        return TimeUnit.MILLISECONDS.toMinutes(abs(System.currentTimeMillis() - timestamp))
    }
}