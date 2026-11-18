package com.lukasz.witkowski.training.planner.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.shared.R
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun Double.formatUpToTwoDecimals(locale: Locale = Locale.getDefault()): String {
    val symbols = DecimalFormatSymbols(locale)
    val formatter = DecimalFormat("#.##", symbols).apply {
        roundingMode = RoundingMode.HALF_UP
    }
    return formatter.format(this)
}

@Composable
fun Double.formatVolumeText(): String = this.takeIf { it > 0.0 }?.formatUpToTwoDecimals()?.let {
    stringResource(R.string.weight_kg_format, it)
} ?: stringResource(R.string.bodyweight)
