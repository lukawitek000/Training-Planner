package com.lukasz.witkowski.shared.utils

fun Double.toPercentage(): String {
    val number = (this * 100).toInt()
    return "$number%"
}