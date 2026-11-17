package com.lukasz.witkowski.training.planner.session.domain.models

import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.shared.utils.calculateCompletenessPercentage
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration

data class ExerciseStatistics(
    val id: ExerciseStatisticsId = ExerciseStatisticsId.create(),
    val trainingExerciseId: TrainingExerciseId,
    val exerciseName: String = "",
    val plannedSets: Int = 1,
    val plannedConfig: SessionExerciseConfiguration = SessionExerciseConfiguration(reps = 1),
    val plannedRestTime: Duration = Duration.ZERO,
    val attemptsStatistics: List<ExerciseAttemptStatistics>
) {
    val totalTime: Duration
        get() = attemptsStatistics.fold(Duration.ZERO) { acc, value -> acc + value.time }

    val completedSetsCount: Int
        get() = attemptsStatistics.count { it.completed }

    val totalSetsCount: Int
        get() = attemptsStatistics.size

    val completenessRate: Double
        get() = calculateCompletenessPercentage(
            completed = completedSetsCount,
            total = totalSetsCount
        )
}
