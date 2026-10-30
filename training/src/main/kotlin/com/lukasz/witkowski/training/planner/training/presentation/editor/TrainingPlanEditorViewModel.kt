package com.lukasz.witkowski.training.planner.training.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingPlanEditorViewModel(
    private val trainingPlanService: TrainingPlanService,
    private val trainingPlanId: TrainingPlanId? = null
) : ViewModel() {
    val isEditMode = trainingPlanId != null
    private val reducer = TrainingConfigurationReducer()
    private val emptyConfiguration = TrainingPlanConfiguration(
        title = "",
        description = "",
        exercises = emptyList(),
        restTime = 90.seconds
    )

    private val _intents = MutableSharedFlow<TrainingPlanEditingIntent>(extraBufferCapacity = 64)

    private val initialConfigurationFlow: Flow<TrainingPlanConfiguration> = if (trainingPlanId != null) {
        trainingPlanService.getTrainingPlanById(trainingPlanId)
            .filterNotNull()
            .take(1)
            .map { plan ->
                TrainingPlanConfiguration(
                    title = plan.title,
                    description = plan.description,
                    exercises = plan.exercises,
                    restTime = plan.restTime
                )
            }
    } else {
        flowOf(emptyConfiguration)
    }

    val uiState: StateFlow<TrainingPlanConfiguration> = initialConfigurationFlow
        .flatMapLatest { initialConfig ->
            _intents.scan(initialConfig) { currentConfig, intent ->
                reducer.reduce(currentConfig, intent)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyConfiguration
        )

    private val _savingResult = Channel<SavingResult>()
    val savingResult = _savingResult.receiveAsFlow()

    fun processIntent(intent: TrainingPlanEditingIntent) {
        if (intent == TrainingPlanEditingIntent.SaveTrainingPlan) {
            saveTrainingPlan()
        } else {
            _intents.tryEmit(intent)
        }
    }

    private fun saveTrainingPlan() {
        viewModelScope.launch {
            val result = if (trainingPlanId != null) {
                trainingPlanService.updateTrainingPlan(uiState.value, trainingPlanId)
            } else {
                trainingPlanService.saveTrainingPlan(uiState.value)
            }
            result
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
