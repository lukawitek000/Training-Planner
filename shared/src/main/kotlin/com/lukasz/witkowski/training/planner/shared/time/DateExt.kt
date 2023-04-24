package com.lukasz.witkowski.training.planner.shared.time

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.formatToString(): String {
    val date = Date(time)
    val format = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
    return format.format(date)
}
