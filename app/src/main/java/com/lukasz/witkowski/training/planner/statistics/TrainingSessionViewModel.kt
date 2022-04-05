package com.lukasz.witkowski.training.planner.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlanMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingSessionViewModel @Inject constructor(
    private val trainingPlanService: TrainingPlanService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _trainingId = savedStateHandle.get<String>("trainingId")
        ?: throw Exception("Training plan id was not provided")
    private val trainingId = TrainingPlanId(_trainingId)

    private val _trainingSessionState =
        MutableStateFlow<TrainingSessionState>(TrainingSessionState.Idle)
    val trainingSessionState: StateFlow<TrainingSessionState>
        get() = _trainingSessionState

    init {
        fetchTrainingPlan()
    }

    private fun fetchTrainingPlan() {
        viewModelScope.launch {
            val trainingPlanDomain = trainingPlanService.getTrainingPlanById(trainingId)
            val trainingPlan = TrainingPlanMapper.toPresentationTrainingPlan(trainingPlanDomain)
            _trainingSessionState.value = TrainingSessionState.TrainingPlanLoadedState(trainingPlan)
        }
    }

    fun startTraining() {

    }
}

sealed interface TrainingSessionState {
    object Idle : TrainingSessionState
    data class TrainingPlanLoadedState(val trainingPlan: TrainingPlan) : TrainingSessionState
    data class ExerciseState(val exercise: TrainingExercise) : TrainingSessionState
    data class RestTimeState(val restTime: Long) : TrainingSessionState
    data class SummaryState(val summary: String) :
        TrainingSessionState // TODO summary objects (Statistics, TrainingPlan??)
}