package com.lukasz.witkowski.shared.models

data class ExerciseStatistics(
    val id: Long = 0L,
    val trainingExerciseId: Long,
    val heartRateStatistics: HeartRateStatistics,
    val burntCaloriesStatistics: CaloriesStatistics,
    val averageTime: Long
)
