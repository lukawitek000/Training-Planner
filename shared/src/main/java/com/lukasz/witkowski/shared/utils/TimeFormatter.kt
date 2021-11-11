package com.lukasz.witkowski.shared.utils

object TimeFormatter {

    const val SECONDS_IN_MINUTE = 60L
    const val MILLIS_IN_SECONDS = 1000L

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
        val timeStringBuilder = java.lang.StringBuilder()
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


    private fun calculateMinutesAndSeconds(millis: Long): Pair<Long, Long> {
        val millisToSeconds = millis / MILLIS_IN_SECONDS
        val minutes = millisToSeconds / 60
        val seconds = millisToSeconds % 60
        return Pair(minutes, seconds)
    }


}
