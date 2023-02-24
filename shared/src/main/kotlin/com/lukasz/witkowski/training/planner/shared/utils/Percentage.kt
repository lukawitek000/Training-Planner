package com.lukasz.witkowski.training.planner.shared.utils

import kotlin.math.roundToInt

fun Double.toPercentage(): String {
    require(this >= 0.0) { "Number cannot be converted to percentage" }
    val number = (this * 100).roundToInt()
    return "$number%"
}
