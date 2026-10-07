package com.lukasz.witkowski.training.planner.training.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.createExercise.TrainingConfigurationReducer
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TrainingPlanEditorViewModel(
    private val trainingPlanService: TrainingPlanService
) : ViewModel() {
    private val reducer = TrainingConfigurationReducer()
    private val emptyConfiguration = TrainingPlanConfiguration(
        title = "",
        description = "",
        exercises = emptyList(),
        restTime = 90.seconds
    )

    val uiState: StateFlow<TrainingPlanConfiguration>
        field = MutableStateFlow(emptyConfiguration)

    private val _savingResult = Channel<SavingResult>()
    val savingResult = _savingResult.receiveAsFlow()

    fun processIntent(intent: TrainingPlanEditingIntent) {
        if (intent == TrainingPlanEditingIntent.SaveTrainingPlan) {
            saveTrainingPlan()
        } else {
            uiState.update { reducer.reduce(it, intent) }
        }
    }

    private fun saveTrainingPlan() {
        viewModelScope.launch {
            trainingPlanService.saveTrainingPlan(uiState.value)
                .onSuccess {
                    Timber.w("Training plan saved: $it")
                    _savingResult.send(SavingResult.Success)
                }.onFailure {
                    Timber.w("Failed to save training plan: ${it.message}")
                    _savingResult.send(SavingResult.Failure(it.message))
                }
        }
    }
}

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
