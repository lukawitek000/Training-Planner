package com.lukasz.witkowski.training.planner.statistics.domain

import java.util.UUID

@JvmInline
value class TrainingSessionId(val value: String) {
    companion object {
        fun create(): TrainingSessionId = TrainingSessionId(UUID.randomUUID().toString())
    }
}