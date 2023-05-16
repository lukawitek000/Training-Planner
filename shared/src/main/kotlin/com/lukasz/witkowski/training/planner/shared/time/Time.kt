package com.lukasz.witkowski.training.planner.shared.time

data class Time(val timeInMillis: Long) {

    constructor(
        hour: Int = 0,
        minutes: Int = 0,
        seconds: Int
    ) : this((((hour * MINUTES_IN_HOUR) + minutes) * SECONDS_IN_MINUTE + seconds) * MILLIS_IN_SECOND)

    fun minutesAndSeconds(millis: Long = timeInMillis): Pair<Int, Int> {
        val millisToSeconds = millis / MILLIS_IN_SECOND
        val minutes = millisToSeconds / SECONDS_IN_MINUTE
        val seconds = millisToSeconds % SECONDS_IN_MINUTE
        return Pair(minutes.toInt(), seconds.toInt())
    }

    fun isNotZero() = timeInMillis > 0L

    fun isZero() = timeInMillis == 0L

    operator fun minus(time: Time) = Time(this.timeInMillis - time.timeInMillis)

    operator fun plus(time: Time) = Time(this.timeInMillis + time.timeInMillis)

    companion object {
        private const val SECONDS_IN_MINUTE = 60L
        private const val MILLIS_IN_SECOND = 1000L
        private const val MINUTES_IN_HOUR = 60L
        val ZERO = Time(0L)
    }
}
