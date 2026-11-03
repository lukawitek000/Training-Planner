package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

internal class SessionStatisticsBuilder(
    private val trainingPlan: TrainingPlan,
    private val startTime: Time,
    private val timeProvider: TimeProvider = SystemTimeProvider()
) {
    private val exercisesAttemptsStatistics = mutableListOf<ExerciseAttemptStatistics>()

    fun addExerciseAttemptStatistics(attemptStatistics: ExerciseAttemptStatistics) {
        exercisesAttemptsStatistics.add(attemptStatistics)
    }

    fun gatherTrainingStatistics(time: Time): TrainingStatistics {
        val exercisesStatisticsMap = exercisesAttemptsStatistics.groupBy { it.trainingExerciseId }
        val exercisesStatistics =
            exercisesStatisticsMap.map { (exerciseId, attemptsStatistics) ->
                ExerciseStatistics(
                    trainingExerciseId = exerciseId,
                    attemptsStatistics = attemptsStatistics
                )
            }
        return TrainingStatistics(
            trainingPlanId = trainingPlan.id,
            totalTime = time - startTime,
            date = timeProvider.currentDate(),
            exercisesStatistics = exercisesStatistics
        )
    }
}
