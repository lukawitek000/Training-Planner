package com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId

/**
 * [BasicStatisticsRecorder] can be used on the device without any sensors, it records:
 *  * Total training time
 *  * Exercise time
 *  * Training session date
 *  * Completeness rate of exercises
 */
class BasicStatisticsRecorder(
    private val trainingPlanId: TrainingPlanId,
    private val timeProvider: TimeProvider
) {

    private var startTrainingTime = Time.NONE
    private var exercisesAttemptsStatistics = mutableListOf<ExerciseAttemptStatistics>()
    private var currentExerciseStartTime = Time.NONE
    private var currentExerciseId: TrainingExerciseId? = null
    private var currentExerciseSet: Int = 0

    fun start() {
        startTrainingTime = timeProvider.currentTime()
    }

    fun stop(): TrainingStatistics {
        val trainingStatistics = gatherTrainingStatistics()
        exercisesAttemptsStatistics.clear()
        return trainingStatistics
    }

    fun startRecordingExercise(trainingExerciseId: TrainingExerciseId, set: Int) {
        currentExerciseStartTime = timeProvider.currentTime()
        currentExerciseId = trainingExerciseId
        currentExerciseSet = set
    }

    fun stopRecordingExercise(isCompleted: Boolean) {
        val exerciseAttemptStatistics = ExerciseAttemptStatistics(
            trainingExerciseId = currentExerciseId!!,
            time = timeProvider.currentTime() - currentExerciseStartTime,
            set = currentExerciseSet,
            completed = isCompleted
        )
        exercisesAttemptsStatistics.add(exerciseAttemptStatistics)
    }

    private fun gatherTrainingStatistics(): TrainingStatistics {
        val exercisesStatisticsMap = exercisesAttemptsStatistics.groupBy { it.trainingExerciseId }
        val exercisesStatistics = exercisesStatisticsMap.map { (exerciseId, attemptsStatistics) ->
            ExerciseStatistics(
                trainingExerciseId = exerciseId,
                attemptsStatistics = attemptsStatistics
            )
        }
        return TrainingStatistics(
            trainingPlanId = trainingPlanId,
            totalTime = timeProvider.currentTime() - startTrainingTime,
            date = timeProvider.currentDate(),
            exercisesStatistics = exercisesStatistics
        )
    }
}
