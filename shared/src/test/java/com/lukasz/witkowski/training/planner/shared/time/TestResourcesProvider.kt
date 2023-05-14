package com.lukasz.witkowski.training.planner.shared.time

import com.lukasz.witkowski.training.planner.shared.R

class TestResourcesProvider: ResourcesProvider {

    override fun provideString(resId: Int): String {
        return when(resId) {
            R.string.seconds_tenth_second_separator -> SECONDS_TENTHS_SEPARATOR
            R.string.minutes_seconds_separator -> MINUTES_SECONDS_SEPARATOR
            else -> throw IllegalArgumentException("Unknown resources id $resId")
        }
    }

    companion object {
        const val MINUTES_SECONDS_SEPARATOR = ":"
        const val SECONDS_TENTHS_SEPARATOR = "."
    }
}