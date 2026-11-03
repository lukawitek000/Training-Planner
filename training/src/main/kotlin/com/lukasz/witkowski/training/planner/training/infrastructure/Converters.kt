package com.lukasz.witkowski.training.planner.training.infrastructure

import androidx.room3.ColumnTypeConverter

object Converters {
    @ColumnTypeConverter
    fun fromStringList(list: List<String>?): String = list?.joinToString(",").orEmpty()

    @ColumnTypeConverter
    fun toStringList(value: String?): List<String> =
        value?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
}
