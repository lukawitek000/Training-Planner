package com.lukasz.witkowski.training.planner.shared.time

import com.lukasz.witkowski.training.planner.shared.R

class TestResourcesProvider : ResourcesProvider {

    override fun provideString(resId: Int): String {
        return when (resId) {
            R.string.seconds_tenth_second_separator -> SECONDS_TENTHS_SEPARATOR
            R.string.minutes_seconds_separator -> MINUTES_SECONDS_SEPARATOR
            R.string.hour_shortcut -> HOUR_SHORTCUT
            R.string.minutes_shortcut -> MINUTE_SHORTCUT
            R.string.seconds_shortcut -> SECOND_SHORTCUT
            else -> throw IllegalArgumentException("Unknown resources id $resId")
        }
    }

    private companion object {
        const val MINUTES_SECONDS_SEPARATOR = ":"
        const val SECONDS_TENTHS_SEPARATOR = "."
        const val HOUR_SHORTCUT = "h"
        const val MINUTE_SHORTCUT = "min"
        const val SECOND_SHORTCUT = "s"
    }
}
