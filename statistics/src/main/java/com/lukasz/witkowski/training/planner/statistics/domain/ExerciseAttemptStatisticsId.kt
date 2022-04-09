package com.lukasz.witkowski.training.planner.statistics.domain

import java.util.UUID

@JvmInline
value class ExerciseAttemptStatisticsId(val value: String) {
    companion object {
        fun create(): ExerciseAttemptStatisticsId = ExerciseAttemptStatisticsId(UUID.randomUUID().toString())
    }
}

