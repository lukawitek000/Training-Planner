package com.lukasz.witkowski.training.planner.analytics.presentation

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


internal fun Double.formatUpToTwoDecimals(locale: Locale = Locale.getDefault()): String {
    val symbols = DecimalFormatSymbols(locale)
    val formatter = DecimalFormat("#.##", symbols).apply {
        roundingMode = RoundingMode.HALF_UP
    }
    return formatter.format(this)
}