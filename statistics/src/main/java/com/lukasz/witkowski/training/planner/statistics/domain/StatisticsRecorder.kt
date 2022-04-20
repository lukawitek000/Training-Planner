package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId

interface StatisticsRecorder {
    val trainingPlanId: TrainingPlanId
    val trainingStatistics: TrainingStatistics
    fun start()
    fun startRecordingExercise(trainingExerciseId: TrainingExerciseId, set: Int)
    fun stopRecordingExercise(isCompleted: Boolean)

}