package com.lukasz.witkowski.shared.models

data class TrainingStatistics(
    val id: Long,
    val trainingId: Long,
    val exerciseStatistics: List<ExerciseStatistics>
)