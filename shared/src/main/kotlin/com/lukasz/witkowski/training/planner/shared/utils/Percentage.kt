package com.lukasz.witkowski.training.planner.shared.utils

import kotlin.math.roundToInt

private const val HUNDRED = 100

fun Double.toPercentage(): String {
    require(this >= 0.0) { "Number cannot be converted to percentage" }
    val number = (this * HUNDRED).roundToInt()
    return "$number%"
}
