package com.lukasz.witkowski.training.planner.statistics.domain.statisticsrecorder

import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId

interface StatisticsRecorder {
    val trainingPlanId: TrainingPlanId
    fun start()
    fun stop(): TrainingStatistics
    fun startRecordingExercise(trainingExerciseId: TrainingExerciseId, set: Int)
    fun stopRecordingExercise(isCompleted: Boolean)
}
