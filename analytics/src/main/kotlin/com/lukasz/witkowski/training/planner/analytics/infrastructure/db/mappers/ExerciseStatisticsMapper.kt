package com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbExerciseStatistics
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbExerciseWithAttemptsStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration.Companion.milliseconds

internal fun ExerciseStatistics.toDbExerciseStatistics(
    trainingStatisticsId: TrainingStatisticsId
): DbExerciseWithAttemptsStatistics = DbExerciseWithAttemptsStatistics(
    exerciseStatistics =
    DbExerciseStatistics(
        id = id.toString(),
        trainingStatisticsId = trainingStatisticsId.toString(),
        trainingExerciseId = trainingExerciseId.toString(),
        exerciseName = exerciseName,
        plannedSets = plannedSets,
        plannedReps = plannedConfig.reps,
        plannedWeightInKg = plannedConfig.weightInKg,
        plannedHoldTime = plannedConfig.holdTime?.inWholeMilliseconds,
        plannedRestTime = plannedRestTime.inWholeMilliseconds
    ),
    exerciseAttemptsStatistics =
    attemptsStatistics.map {
        it.toDbExerciseAttemptStatistics(id)
    }
)

internal fun DbExerciseWithAttemptsStatistics.toExerciseStatistics(): ExerciseStatistics =
    ExerciseStatistics(
        id = ExerciseStatisticsId(exerciseStatistics.id),
        trainingExerciseId = TrainingExerciseId(exerciseStatistics.trainingExerciseId),
        exerciseName = exerciseStatistics.exerciseName,
        plannedSets = exerciseStatistics.plannedSets,
        plannedConfig = SessionExerciseConfiguration(
            reps = exerciseStatistics.plannedReps,
            weightInKg = exerciseStatistics.plannedWeightInKg,
            holdTime = exerciseStatistics.plannedHoldTime?.milliseconds
        ),
        plannedRestTime = exerciseStatistics.plannedRestTime.milliseconds,
        attemptsStatistics =
        exerciseAttemptsStatistics
            .sortedBy { it.set }
            .map { it.toExerciseAttemptStatistics() }
    )
