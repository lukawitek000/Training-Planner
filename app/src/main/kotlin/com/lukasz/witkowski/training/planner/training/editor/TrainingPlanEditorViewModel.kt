package com.lukasz.witkowski.training.planner.training.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.createExercise.TrainingConfigurationReducer
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration

class TrainingPlanEditorViewModel(
    private val trainingPlanService: TrainingPlanService
) : ViewModel() {
    private val reducer = TrainingConfigurationReducer()
    private val emptyConfiguration = TrainingPlanConfiguration(
        title = "",
        description = "",
        exercises = emptyList(),
        restTime = Duration.ZERO
    )

    val uiState: StateFlow<TrainingPlanConfiguration>
        field = MutableStateFlow(emptyConfiguration)

    fun processIntent(intent: TrainingPlanEditingIntent) {
        uiState.update {
            reducer.reduce(it, intent).also { result ->
                Timber.i("Intent: $intent, processed.")
                Timber.i("UiState: $result")
            }
        }
    }
}

sealed interface TrainingPlanEditorUiState {
    data class Editing(
        val configuration: TrainingPlanConfiguration,
    ): TrainingPlanEditorUiState
}

sealed interface TrainingPlanEditingIntent {
    data class TitleChanged(val title: String): TrainingPlanEditingIntent
    data class DescriptionChanged(val description: String): TrainingPlanEditingIntent
    data class RestTimeChanged(val restTime: Duration): TrainingPlanEditingIntent
    data class TrainingExerciseAdded(val trainingExercise: TrainingExercise): TrainingPlanEditingIntent
    data class TrainingExerciseRemoved(val trainingExercise: TrainingExercise): TrainingPlanEditingIntent
    data class TrainingExercisesReordered(val fromIndex: Int, val toIndex: Int): TrainingPlanEditingIntent
}
