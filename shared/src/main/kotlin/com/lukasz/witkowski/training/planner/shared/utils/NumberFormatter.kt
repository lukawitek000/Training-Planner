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
fun WeightUnit.shortLabel(): String = when (this) {
    WeightUnit.KG -> stringResource(R.string.unit_kg_short)
    WeightUnit.LBS -> stringResource(R.string.unit_lbs_short)
}

@Composable
fun Weight.formatWeightText(weightUnit: WeightUnit = LocalWeightUnit.current): String {
    val converted = toDouble(weightUnit)
    val formatted = converted.formatUpToTwoDecimals()
    val formatRes = when (weightUnit) {
        WeightUnit.KG -> R.string.weight_kg_format
        WeightUnit.LBS -> R.string.weight_lbs_format
    }
    return stringResource(formatRes, formatted)
}

@Composable
fun Weight?.formatVolumeText(weightUnit: WeightUnit = LocalWeightUnit.current): String =
    this?.takeIf { it.isPositive }?.formatWeightText(weightUnit)
        ?: stringResource(R.string.bodyweight)

@Composable
fun Double.formatWeightText(weightUnit: WeightUnit = LocalWeightUnit.current): String =
    Weight.ofKilograms(this).formatWeightText(weightUnit)

@Composable
fun Double.formatVolumeText(weightUnit: WeightUnit = LocalWeightUnit.current): String =
    Weight.ofKilograms(this).formatVolumeText(weightUnit)
