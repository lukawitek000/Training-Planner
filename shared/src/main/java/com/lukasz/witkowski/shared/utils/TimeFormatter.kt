package com.lukasz.witkowski.shared.utils

object TimeFormatter {

    const val SECONDS_IN_MINUTE = 60
    const val MILLIS_IN_SECONDS = 1000

    fun millisToMinutesSeconds(millis: Long) : String {
        val millisToSeconds = millis / MILLIS_IN_SECONDS
        val minutes = millisToSeconds / 60
        val seconds = millisToSeconds % 60
        val timeStringBuilder = StringBuilder()
        if(minutes != 0L){
            timeStringBuilder.append("$minutes min ")
        }
        if(seconds != 0L){
            timeStringBuilder.append("$seconds s")
        }
        return timeStringBuilder.toString()
    }
}
