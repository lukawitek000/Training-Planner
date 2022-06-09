package com.lukasz.witkowski.training.planner.exercise.domain

import java.util.UUID

@JvmInline
//Adrian: why not to pass UUID?
value class ExerciseId(val value: String) {
    companion object {
        fun create(): ExerciseId = ExerciseId(UUID.randomUUID().toString())
    }
}
