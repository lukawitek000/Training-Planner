package com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date

class BasicStatisticsRecorder(
    override val trainingPlanId: TrainingPlanId,
    private val timeProvider: TimeProvider
) : StatisticsRecorder {

    private var startTrainingTime = Time.NONE
    private var exercisesAttemptsStatistics = mutableListOf<ExerciseAttemptStatistics>()
    private var currentExerciseStartTime = Time.NONE
    private var currentExerciseId: TrainingExerciseId? = null
    private var currentExerciseSet: Int = 0

    override fun start() {
        startTrainingTime = timeProvider.currentTime()
    }

    override fun stop(): TrainingStatistics {
        val trainingStatistics = gatherTrainingStatistics()
        exercisesAttemptsStatistics.clear()
        return trainingStatistics
    }

    override fun startRecordingExercise(trainingExerciseId: TrainingExerciseId, set: Int) {
        currentExerciseStartTime = timeProvider.currentTime()
        currentExerciseId = trainingExerciseId
        currentExerciseSet = set
    }

    override fun stopRecordingExercise(isCompleted: Boolean) {
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
            date = Date(),
            exercisesStatistics = exercisesStatistics
        )
    }
}