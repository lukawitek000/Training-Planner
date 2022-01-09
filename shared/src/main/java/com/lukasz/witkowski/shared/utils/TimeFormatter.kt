package com.lukasz.witkowski.shared.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeFormatter {

    const val SECONDS_IN_MINUTE = 60L
    const val MILLIS_IN_SECOND = 1000L
    const val MINUTES_IN_HOUR = 60L

    fun millisToMinutesSeconds(millis: Long): String {
        val (minutes, seconds) = calculateMinutesAndSeconds(millis)
        val timeStringBuilder = StringBuilder()
        if (minutes != 0) {
            timeStringBuilder.append("$minutes min ")
        }
        if (seconds != 0) {
            timeStringBuilder.append("$seconds s")
        }
        return timeStringBuilder.toString()
    }

    fun millisToTimer(millis: Long): String {
        val (minutes, seconds) = calculateMinutesAndSeconds(millis)
        val timeStringBuilder = StringBuilder()
        if (minutes < 10) {
            timeStringBuilder.append("0$minutes")
        } else {
            timeStringBuilder.append(minutes)
        }
        timeStringBuilder.append(":")
        if(seconds < 10) {
            timeStringBuilder.append("0$seconds")
        } else {
            timeStringBuilder.append(seconds)
        }
        return timeStringBuilder.toString()
    }

    fun millisToTime(millis: Long): String {
        val (minutes, seconds) = calculateMinutesAndSeconds(millis)
        val hours = millis / MILLIS_IN_SECOND / SECONDS_IN_MINUTE / MINUTES_IN_HOUR
        val timeStringBuilder = StringBuilder()
        if(hours > 0) {
            timeStringBuilder.append("${hours}h")
        }
        if(minutes > 0) {
            timeStringBuilder.append(" ")
            timeStringBuilder.append("${minutes}min")
        }
        if(seconds > 0 && hours <= 0) {
            timeStringBuilder.append(" ")
            timeStringBuilder.append("${seconds}s")
        }
        if(timeStringBuilder.isEmpty()) {
            timeStringBuilder.append("0s")
        }
        return timeStringBuilder.toString()
    }

    fun timeToMillis(hour: Int = 0, minutes: Int = 0, seconds: Int): Long {
         return (((hour * MINUTES_IN_HOUR) + minutes) * SECONDS_IN_MINUTE + seconds) * MILLIS_IN_SECOND
    }

    fun calculateMinutesAndSeconds(millis: Long): Pair<Int, Int> {
        val millisToSeconds = millis / MILLIS_IN_SECOND
        val minutes = millisToSeconds / SECONDS_IN_MINUTE
        val seconds = millisToSeconds % SECONDS_IN_MINUTE
        return Pair(minutes.toInt(), seconds.toInt())
    }

    fun convertMillisToDate(millis: Long): String {
        val date = Date(millis)
        val format = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
        return format.format(date)
    }
}
