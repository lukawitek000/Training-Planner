package com.lukasz.witkowski.shared.utils

import java.util.concurrent.TimeUnit

object TimeFormatter {

    const val SECONDS_IN_MINUTE = 60L
    const val MILLIS_IN_SECONDS = 1000L
    const val MINUTES_IN_HOUR = 60L

    fun millisToMinutesSeconds(millis: Long): String {
        val (minutes, seconds) = calculateMinutesAndSeconds(millis)
        val timeStringBuilder = StringBuilder()
        if (minutes != 0L) {
            timeStringBuilder.append("$minutes min ")
        }
        if (seconds != 0L) {
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
        val hours = millis / MILLIS_IN_SECONDS / SECONDS_IN_MINUTE / MINUTES_IN_HOUR
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
        return timeStringBuilder.toString()
    }


    private fun calculateMinutesAndSeconds(millis: Long): Pair<Long, Long> {
        val millisToSeconds = millis / MILLIS_IN_SECONDS
        val minutes = millisToSeconds / SECONDS_IN_MINUTE
        val seconds = millisToSeconds % SECONDS_IN_MINUTE
        return Pair(minutes, seconds)
    }


}
