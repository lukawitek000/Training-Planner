package com.lukasz.witkowski.training.planner.statistics.domain.models

import java.util.UUID

@JvmInline
value class ExerciseAttemptStatisticsId(val value: UUID) {

    constructor(name: String): this(UUID.fromString(name))

    override fun toString(): String = value.toString()

    companion object {
        fun create(): ExerciseAttemptStatisticsId = ExerciseAttemptStatisticsId(UUID.randomUUID())
    }
}
