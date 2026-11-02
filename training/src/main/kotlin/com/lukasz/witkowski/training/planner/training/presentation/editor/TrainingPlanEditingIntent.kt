package com.lukasz.witkowski.training.planner.training.presentation.editor

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import kotlin.time.Duration

sealed interface SavingResult {
    data object Success : SavingResult
    data class Failure(val message: String?) : SavingResult
}

sealed interface TrainingPlanEditingIntent {
    data class TitleChanged(val title: String) : TrainingPlanEditingIntent
    data class DescriptionChanged(val description: String) : TrainingPlanEditingIntent
    data class RestTimeChanged(val restTime: Duration) : TrainingPlanEditingIntent
    data class TrainingExerciseAdded(val trainingExercise: TrainingExercise) :
        TrainingPlanEditingIntent

    data class TrainingExerciseRemoved(val trainingExercise: TrainingExercise) :
        TrainingPlanEditingIntent

    data class TrainingExercisesReordered(val fromIndex: Int, val toIndex: Int) :
        TrainingPlanEditingIntent

    data object SaveTrainingPlan : TrainingPlanEditingIntent
}
