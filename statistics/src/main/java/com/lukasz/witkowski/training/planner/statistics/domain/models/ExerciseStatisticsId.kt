package com.lukasz.witkowski.training.planner.statistics.domain.models

import java.util.UUID

@JvmInline
value class ExerciseStatisticsId(val value: String) {
    companion object {
        fun create(): ExerciseStatisticsId = ExerciseStatisticsId(UUID.randomUUID().toString())
    }
}
