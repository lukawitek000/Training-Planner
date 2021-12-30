package com.lukasz.witkowski.shared.models

data class ExerciseStatistics(
    val id: Long,
    val trainingExerciseId: Long,
    val heartRateStatistics: HeartRateStatistics,
    val burntCaloriesStatistics: CaloriesStatistics
)
