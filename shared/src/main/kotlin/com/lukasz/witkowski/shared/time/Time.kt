package com.lukasz.witkowski.shared.time

data class Time(val timeInMillis: Long) {

    constructor(
        hour: Int = 0,
        minutes: Int = 0,
        seconds: Int
    ) : this((((hour * MINUTES_IN_HOUR) + minutes) * SECONDS_IN_MINUTE + seconds) * MILLIS_IN_SECOND)

    fun toTimerString(includeTenthSecond: Boolean = true): String {
        val (minutes, seconds) = calculateMinutesAndSeconds()
        val timeStringBuilder = StringBuilder()
        if (minutes > 0) timeStringBuilder.append(minutes).append(":")
        appendZeroBeforeSecondDigitIfNeeded(seconds, timeStringBuilder)
        timeStringBuilder.append(seconds)
        if (includeTenthSecond) {
            appendTenthSecond(minutes, seconds, timeStringBuilder)
        }
        return timeStringBuilder.toString()
    }

    override fun toString(): String {
        val hours = timeInMillis / MILLIS_IN_HOUR
        val remainingTime = timeInMillis - hours * MILLIS_IN_HOUR
        val (minutes, seconds) = calculateMinutesAndSeconds(remainingTime)
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

    fun calculateMinutesAndSeconds(millis: Long = timeInMillis): Pair<Int, Int> {
        val millisToSeconds = millis / MILLIS_IN_SECOND
        val minutes = millisToSeconds / SECONDS_IN_MINUTE
        val seconds = millisToSeconds % SECONDS_IN_MINUTE
        return Pair(minutes.toInt(), seconds.toInt())
    }

    fun isNotZero() = timeInMillis > 0L

    fun isZero() = timeInMillis == 0L

    operator fun minus(time: Time) = Time(this.timeInMillis - time.timeInMillis)

    operator fun plus(time: Time) = Time(this.timeInMillis + time.timeInMillis)

    private fun appendZeroBeforeSecondDigitIfNeeded(
        seconds: Int,
        timeStringBuilder: StringBuilder
    ) {
        if (timeStringBuilder.isNotEmpty() && seconds in 0..9) {
            timeStringBuilder.append(0)
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

    private fun StringBuilder.appendSpaceIfNotEmpty() {
        if (isNotEmpty()) append(" ")
    }

    companion object {
        private const val SECONDS_IN_MINUTE = 60L
        private const val MILLIS_IN_SECOND = 1000L
        private const val MINUTES_IN_HOUR = 60L
        private const val MILLIS_IN_CENTISECOND = 100L
        private const val MILLIS_IN_MINUTE = SECONDS_IN_MINUTE * MILLIS_IN_SECOND
        private const val MILLIS_IN_HOUR = MINUTES_IN_HOUR * MILLIS_IN_MINUTE
        val ZERO = Time(0L)
    }
}
