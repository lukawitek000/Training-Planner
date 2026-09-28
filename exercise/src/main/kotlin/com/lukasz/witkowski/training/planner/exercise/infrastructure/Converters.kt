package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.ColumnTypeConverter

object Converters {
    @ColumnTypeConverter
    fun fromRecommendationLevel(level: RecommendationLevel): String {
        return level.name
    }

    @ColumnTypeConverter
    fun toRecommendationLevel(value: String): RecommendationLevel {
        return RecommendationLevel.valueOf(value)
    }
}