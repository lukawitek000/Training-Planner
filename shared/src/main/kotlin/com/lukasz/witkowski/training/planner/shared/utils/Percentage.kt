package com.lukasz.witkowski.training.planner.shared.utils

import kotlin.math.roundToInt

private const val HUNDRED = 100
private const val HUNDRED_DOUBLE = 100.0

fun Double.toPercentage(): String {
    require(this >= 0.0) { "Number cannot be converted to percentage" }
    val number = (this * HUNDRED).roundToInt()
    return "$number%"
}

fun calculateCompletenessPercentage(completed: Int, total: Int): Double =
    if (total == 0) 0.0 else (completed.toDouble() / total.toDouble()) * HUNDRED_DOUBLE
