package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId

data class ExerciseAttemptStatistics(
    val id: ExerciseAttemptStatisticsId = ExerciseAttemptStatisticsId.create(),
    val trainingExerciseId: TrainingExerciseId,
    val time: Time,
    val set: Int,
    val completed: Boolean
)
