package com.lukasz.witkowski.training.planner.statistics.domain.session

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics as Statistics
import java.util.Date

class TrainingStatistics(
    private val trainingPlan: TrainingPlan,
    private val startTime: Time
) {

    private var exercisesAttemptsStatistics = mutableListOf<ExerciseAttemptStatistics>()

    fun addExerciseAttemptStatistics(attemptStatistics: ExerciseAttemptStatistics) {
        exercisesAttemptsStatistics.add(attemptStatistics)
    }

    fun gatherTrainingStatistics(time: Time): Statistics {
        val exercisesStatisticsMap = exercisesAttemptsStatistics.groupBy { it.trainingExerciseId }
        val exercisesStatistics = exercisesStatisticsMap.map { (exerciseId, attemptsStatistics) ->
            ExerciseStatistics(
                trainingExerciseId = exerciseId,
                attemptsStatistics = attemptsStatistics
            )
        }
        return Statistics(
            trainingPlanId = trainingPlan.id,
            totalTime = time - startTime,
            date = Date(), // TODO how to provide a date
            exercisesStatistics = exercisesStatistics
        )
    }
}