package com.lukasz.witkowski.training.planner.shared.time

import android.content.Context
import com.lukasz.witkowski.training.planner.shared.R

class TimeFormatter internal constructor(private val resourcesProvider: ResourcesProvider) {

    constructor(context: Context) : this(SystemResourcesProvider(context))

    fun formatTime(time: Time): String {
        val hours = (time.timeInMillis / MILLIS_IN_HOUR).toInt()
        val remainingTime = time.timeInMillis - hours * MILLIS_IN_HOUR
        val (minutes, seconds) = time.minutesAndSeconds(remainingTime)
        return StringBuilder()
            .appendHoursTime(hours)
            .appendMinutesTime(minutes)
            .appendSecondsTime(seconds, hours)
            .toString()
    }

    fun formatTimer(time: Time, includeTenthSecond: Boolean = true): String {
        val (minutes, seconds) = time.minutesAndSeconds()
        return StringBuilder()
            .appendMinutes(minutes)
            .appendSeparator(resourcesProvider.provideString(R.string.minutes_seconds_separator))
            .appendSeconds(seconds)
            .apply {
                if (includeTenthSecond) {
                    appendTenthSecond(
                        time,
                        resourcesProvider.provideString(R.string.seconds_tenth_second_separator)
                    )
                }
            }.toString()
    }

    private fun StringBuilder.appendMinutes(minutes: Int) = apply {
        if (minutes > 0) {
            append(minutes)
        }
    }

    private fun StringBuilder.appendSeparator(separator: String) = apply {
        if (isNotBlank()) {
            append(separator)
        }
    }

    private fun StringBuilder.appendSeconds(seconds: Int) = apply {
        if (isNotBlank() && seconds in DIGIT_RANGE) {
            append(0)
        }
        append(seconds)
    }

    private fun StringBuilder.appendTenthSecond(time: Time, separator: String) = apply {
        val (minutes, seconds) = time.minutesAndSeconds()
        val millis =
            time.timeInMillis - minutes * MILLIS_IN_MINUTE - seconds * MILLIS_IN_SECOND
        val tenthSecond = millis / MILLIS_IN_CENTISECOND
        append(separator)
        append(tenthSecond)
    }

    private fun StringBuilder.appendHoursTime(hours: Int) = apply {
        if (hours > 0) {
            append(hours)
            appendSeparator("h")
        }
    }

    private fun StringBuilder.appendMinutesTime(minutes: Int) = apply {
        if (minutes > 0) {
            if (isNotBlank()) {
                appendSpace()
            }
            append(minutes)
            appendSeparator("min")
        }
    }

    private fun StringBuilder.appendSecondsTime(seconds: Int, hours: Int) = apply {
        if ((seconds > 0 && hours <= 0) || isEmpty()) {
            if (isNotBlank()) {
                appendSpace()
            }
            append(seconds)
            appendSeparator("s")
        }
    }

    private fun StringBuilder.appendSpace() = apply {
        if (isNotBlank()) {
            append(" ")
        }
    }

    companion object {
        private const val SECONDS_IN_MINUTE = 60L
        private const val MILLIS_IN_SECOND = 1000L
        private const val MILLIS_IN_CENTISECOND = 100L
        private const val MILLIS_IN_MINUTE = SECONDS_IN_MINUTE * MILLIS_IN_SECOND
        private const val MINUTES_IN_HOUR = 60L
        private const val MILLIS_IN_HOUR = MINUTES_IN_HOUR * MILLIS_IN_MINUTE
        private val DIGIT_RANGE = 0..9
    }
}
