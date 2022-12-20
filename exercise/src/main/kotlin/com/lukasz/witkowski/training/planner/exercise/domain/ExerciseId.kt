package com.lukasz.witkowski.training.planner.exercise.domain

import java.util.UUID

@JvmInline
value class ExerciseId(val value: UUID) {
    constructor(name: String): this(UUID.fromString(name))

    companion object {
        fun create(): ExerciseId = ExerciseId(UUID.randomUUID())
    }

    override fun toString(): String = value.toString()
}
