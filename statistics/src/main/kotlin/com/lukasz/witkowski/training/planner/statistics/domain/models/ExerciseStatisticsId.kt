package com.lukasz.witkowski.training.planner.statistics.domain.models

import java.util.UUID

@JvmInline
value class ExerciseStatisticsId(val value: UUID) {

    constructor(name: String): this(UUID.fromString(name))

    override fun toString(): String = value.toString()

    companion object {
        fun create(): ExerciseStatisticsId = ExerciseStatisticsId(UUID.randomUUID())
    }
}
