package com.lukasz.witkowski.training.planner.shared.time

data class Time(val timeInMillis: Long) {

    constructor(
        hour: Int = 0,
        minutes: Int = 0,
        seconds: Int
    ) : this((((hour * MINUTES_IN_HOUR) + minutes) * SECONDS_IN_MINUTE + seconds) * MILLIS_IN_SECOND)

    override fun toString(): String {
        val hours = timeInMillis / MILLIS_IN_HOUR
        val remainingTime = timeInMillis - hours * MILLIS_IN_HOUR
        val (minutes, seconds) = minutesAndSeconds(remainingTime)
        val timeStringBuilder = StringBuilder()
        if (hours > 0) timeStringBuilder.append("${hours}h")
        if (minutes > 0) {
            timeStringBuilder.appendSpaceIfNotEmpty()
            timeStringBuilder.append("${minutes}min")
        }
        if ((seconds > 0 && hours <= 0) || timeStringBuilder.isEmpty()) {
            timeStringBuilder.appendSpaceIfNotEmpty()
            timeStringBuilder.append("${seconds}s")
        }
        return timeStringBuilder.toString()
    }

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


    private fun StringBuilder.appendSpaceIfNotEmpty() {
        if (isNotEmpty()) append(" ")
    }

    companion object {
        private const val SECONDS_IN_MINUTE = 60L
        private const val MILLIS_IN_SECOND = 1000L
        private const val MINUTES_IN_HOUR = 60L
        private const val MILLIS_IN_MINUTE = SECONDS_IN_MINUTE * MILLIS_IN_SECOND
        private const val MILLIS_IN_HOUR = MINUTES_IN_HOUR * MILLIS_IN_MINUTE
        val ZERO = Time(0L)
    }
}
