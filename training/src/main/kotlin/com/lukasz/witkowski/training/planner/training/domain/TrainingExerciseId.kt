package com.lukasz.witkowski.training.planner.training.domain

import java.util.UUID

@JvmInline
value class TrainingExerciseId(val value: UUID) {

    constructor(name: String) : this(UUID.fromString(name))

    override fun toString(): String = value.toString()

    companion object {
        fun create(): TrainingExerciseId = TrainingExerciseId(UUID.randomUUID())
    }
}
