package com.lukasz.witkowski.training.planner.statistics.domain.models

import java.util.UUID

@JvmInline
value class ExerciseAttemptStatisticsId(val value: String) {
    companion object {
        fun create(): ExerciseAttemptStatisticsId =
            ExerciseAttemptStatisticsId(UUID.randomUUID().toString())
    }
}
