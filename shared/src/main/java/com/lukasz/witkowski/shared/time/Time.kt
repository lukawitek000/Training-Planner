package com.lukasz.witkowski.shared.time

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Time(val timeInMillis: Long) {

    constructor(
        hour: Int = 0,
        minutes: Int = 0,
        seconds: Int
    ) : this((((hour * MINUTES_IN_HOUR) + minutes) * SECONDS_IN_MINUTE + seconds) * MILLIS_IN_SECOND)

    fun toTimerString(): String {
        val (minutes, seconds) = calculateMinutesAndSeconds()
        val timeStringBuilder = StringBuilder()
        if (minutes < 10) {
            timeStringBuilder.append("0$minutes")
        } else {
            timeStringBuilder.append(minutes)
        }
        timeStringBuilder.append(":")
        if (seconds < 10) {
            timeStringBuilder.append("0$seconds")
        } else {
            timeStringBuilder.append(seconds)
        }
        return timeStringBuilder.toString()
    }

    override fun toString(): String {
        val hours = timeInMillis / MILLIS_IN_HOUR
        val remainingTime = timeInMillis - hours * MILLIS_IN_HOUR
        val (minutes, seconds) = calculateMinutesAndSeconds(remainingTime)
        val timeStringBuilder = StringBuilder()
        if (hours > 0) {
            timeStringBuilder.append("${hours}h")
        }
        if (minutes > 0) {
            timeStringBuilder.append(" ")
            timeStringBuilder.append("${minutes}min")
        }
        if (seconds > 0 && hours <= 0) {
            timeStringBuilder.append(" ")
            timeStringBuilder.append("${seconds}s")
        }
//        if (timeStringBuilder.isEmpty()) {
//            timeStringBuilder.append("0s")
//        }
        return timeStringBuilder.toString()
    }


    fun toDateString(): String {
        val date = Date(timeInMillis)
        val format = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
        return format.format(date)
    }

    fun calculateMinutesAndSeconds(millis: Long = timeInMillis): Pair<Int, Int> {
        val millisToSeconds = millis / MILLIS_IN_SECOND
        val minutes = millisToSeconds / SECONDS_IN_MINUTE
        val seconds = millisToSeconds % SECONDS_IN_MINUTE
        return Pair(minutes.toInt(), seconds.toInt())
    }

    fun isNotZero() = timeInMillis > 0L

    companion object {
        private const val SECONDS_IN_MINUTE = 60L
        private const val MILLIS_IN_SECOND = 1000L
        private const val MINUTES_IN_HOUR = 60L
        private const val MILLIS_IN_HOUR = MINUTES_IN_HOUR * SECONDS_IN_MINUTE * MILLIS_IN_SECOND
        val NONE = Time(0L)
    }
}