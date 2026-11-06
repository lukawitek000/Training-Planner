package com.lukasz.witkowski.training.planner.session.domain.models

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration

data class ExerciseAttemptStatistics(
    val id: ExerciseAttemptStatisticsId = ExerciseAttemptStatisticsId.create(),
    val trainingExerciseId: TrainingExerciseId,
    val set: Int,
    val completed: Boolean,
    val actualReps: Int = 0,
    val actualWeightInKg: Double? = null,
    val actualTime: Duration? = null,
    val rpe: Float? = null
)
