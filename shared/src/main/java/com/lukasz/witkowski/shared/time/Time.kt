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
        timeStringBuilder.appendWithZeroBeforeNumberIfLessThan10(minutes)
        timeStringBuilder.append(":")
        timeStringBuilder.appendWithZeroBeforeNumberIfLessThan10(seconds)
        appendTenthSecond(minutes, seconds, timeStringBuilder)
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
            if(timeStringBuilder.isNotEmpty()) {
                timeStringBuilder.append(" ")
            }
            timeStringBuilder.append("${minutes}min")
        }
        if (seconds > 0 && hours <= 0) {
            if(timeStringBuilder.isNotEmpty()) {
                timeStringBuilder.append(" ")
            }
            timeStringBuilder.append("${seconds}s")
        }
        if (timeStringBuilder.isEmpty()) {
            timeStringBuilder.append("${seconds}s")
        }
        return timeStringBuilder.toString()
    }

    override fun equals(other: Any?): Boolean {
        return timeInMillis == (other as? Time)?.timeInMillis
    }

    fun calculateMinutesAndSeconds(millis: Long = timeInMillis): Pair<Int, Int> {
        val millisToSeconds = millis / MILLIS_IN_SECOND
        val minutes = millisToSeconds / SECONDS_IN_MINUTE
        val seconds = millisToSeconds % SECONDS_IN_MINUTE
        return Pair(minutes.toInt(), seconds.toInt())
    }

    fun isNotZero() = timeInMillis > 0L

    operator fun minus(time: Time) = Time(this.timeInMillis - time.timeInMillis)

    private fun StringBuilder.appendWithZeroBeforeNumberIfLessThan10(number: Int) {
        if (number < 10) {
            append("0$number")
        } else {
            append(number)
        }
    }

    private fun appendTenthSecond(
        minutes: Int,
        seconds: Int,
        timeStringBuilder: StringBuilder
    ) {
        val millis = timeInMillis - minutes * MILLIS_IN_MINUTE - seconds * MILLIS_IN_SECOND
        val tenthSecond = millis / MILLIS_IN_CENTISECOND
        timeStringBuilder.append(".$tenthSecond")
    }

    companion object {
        private const val SECONDS_IN_MINUTE = 60L
        private const val MILLIS_IN_SECOND = 1000L
        private const val MINUTES_IN_HOUR = 60L
        private const val MILLIS_IN_CENTISECOND = 100L
        private const val MILLIS_IN_MINUTE = SECONDS_IN_MINUTE * MILLIS_IN_SECOND
        private const val MILLIS_IN_HOUR = MINUTES_IN_HOUR * MILLIS_IN_MINUTE
        val NONE = Time(0L)
    }
}