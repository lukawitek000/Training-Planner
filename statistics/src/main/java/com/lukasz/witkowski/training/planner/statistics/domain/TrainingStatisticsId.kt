package com.lukasz.witkowski.training.planner.statistics.domain

import java.util.UUID

@JvmInline
value class TrainingStatisticsId(val value: String) {
    companion object {
        fun create(): TrainingStatisticsId = TrainingStatisticsId(UUID.randomUUID().toString())
    }
}