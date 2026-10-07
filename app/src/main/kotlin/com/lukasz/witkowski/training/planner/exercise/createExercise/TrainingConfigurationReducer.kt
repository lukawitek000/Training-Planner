package com.lukasz.witkowski.training.planner.exercise.createExercise

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import com.lukasz.witkowski.training.planner.training.editor.TrainingPlanEditingIntent
import timber.log.Timber

class TrainingConfigurationReducer {
    fun reduce(currentState: TrainingPlanConfiguration, intent: TrainingPlanEditingIntent): TrainingPlanConfiguration {
        Timber.i("Reduce intent: $intent")
        return when (intent) {
            is TrainingPlanEditingIntent.TitleChanged -> currentState.copy(
                title = intent.title
            )
            is TrainingPlanEditingIntent.DescriptionChanged -> currentState.copy(
                description = intent.description
            )
            is TrainingPlanEditingIntent.RestTimeChanged -> currentState.copy(
                restTime = intent.restTime
            )
            is TrainingPlanEditingIntent.TrainingExerciseAdded -> currentState.copy(
                exercises = currentState.exercises + intent.trainingExercise
            )
            is TrainingPlanEditingIntent.TrainingExerciseRemoved -> currentState.copy(
                exercises = currentState.exercises - intent.trainingExercise
            )
        }
    }
}
