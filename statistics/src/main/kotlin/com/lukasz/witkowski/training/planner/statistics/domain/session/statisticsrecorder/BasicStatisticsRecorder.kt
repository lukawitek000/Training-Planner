package com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date

/**
 * [BasicStatisticsRecorder] can be used on the device without any sensors, it records:
 *  * Total training time
 *  * Exercise time
 *  * Training session date
 *  * Completeness rate of exercises
 */
class BasicStatisticsRecorder(
    private val trainingPlanId: TrainingPlanId
) {

    private var startTrainingTime = Time.ZERO
    private var exercisesAttemptsStatistics = mutableListOf<ExerciseAttemptStatistics>()
    private var currentExerciseStartTime = Time.ZERO
    private var currentExerciseId: TrainingExerciseId? = null
    private var currentExerciseSet: Int = 0

    fun start(startTime: Time) {
        startTrainingTime = startTime
    }

    fun stop(stopTime: Time): TrainingStatistics {
        val trainingStatistics = gatherTrainingStatistics(stopTime)
        exercisesAttemptsStatistics.clear()
        return trainingStatistics
    }

    fun startRecordingExercise(trainingExerciseId: TrainingExerciseId, set: Int, startTime: Time) {
        currentExerciseStartTime = startTime
        currentExerciseId = trainingExerciseId
        currentExerciseSet = set
    }

    fun stopRecordingExercise(isCompleted: Boolean, stopTime: Time) {
        val exerciseAttemptStatistics = ExerciseAttemptStatistics(
            trainingExerciseId = currentExerciseId!!,
            time = stopTime - currentExerciseStartTime,
            set = currentExerciseSet,
            completed = isCompleted
        )
        exercisesAttemptsStatistics.add(exerciseAttemptStatistics)
    }

    private fun gatherTrainingStatistics(time: Time): TrainingStatistics {
        val exercisesStatisticsMap = exercisesAttemptsStatistics.groupBy { it.trainingExerciseId }
        val exercisesStatistics = exercisesStatisticsMap.map { (exerciseId, attemptsStatistics) ->
            ExerciseStatistics(
                trainingExerciseId = exerciseId,
                attemptsStatistics = attemptsStatistics
            )
        }
        return TrainingStatistics(
            trainingPlanId = trainingPlanId,
            totalTime = time - startTrainingTime,
            date = Date(), // TODO how to provide a date
            exercisesStatistics = exercisesStatistics
        )
    }
}
