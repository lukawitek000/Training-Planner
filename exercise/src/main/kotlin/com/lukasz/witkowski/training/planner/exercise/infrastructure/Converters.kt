package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.ColumnTypeConverter

object Converters {
    @ColumnTypeConverter
    fun fromRecommendationLevel(level: RecommendationLevel): String = level.name

    @ColumnTypeConverter
    fun toRecommendationLevel(value: String): RecommendationLevel = RecommendationLevel.valueOf(value)
}
