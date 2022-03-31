package com.lukasz.witkowski.training.planner.training.domain

import java.util.UUID

@JvmInline
value class TrainingPlanId(val value: String) {
    companion object {
        fun create(): TrainingPlanId = TrainingPlanId(UUID.randomUUID().toString())
    }
}
