package com.lukasz.witkowski.training.planner.training.infrastructure

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant

object InstantConverters {
    @ColumnTypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilliseconds()

    @ColumnTypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }
}
