package com.lukasz.witkowski.training.planner.training.domain

import java.util.UUID

@JvmInline
value class TrainingExerciseId(val value: String) {
    companion object {
        fun create(): TrainingExerciseId = TrainingExerciseId(UUID.randomUUID().toString())
    }
}