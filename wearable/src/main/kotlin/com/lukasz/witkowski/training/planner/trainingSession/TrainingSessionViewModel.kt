package com.lukasz.witkowski.training.planner.trainingSession

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.dummyTrainingsList
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.presentation.toPresentationTrainingSessionState
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.mappers.toDomainTrainingPlan
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrainingSessionViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val trainingPlanService: TrainingPlanService,
    private val trainingSessionService: TrainingSessionService,
) : ViewModel() {

    private val _trainingPlanId =
        savedStateHandle.get<String>(StartTrainingActivity.TRAINING_ID_KEY)
            ?: throw IllegalStateException("Missing training id")
    private val trainingPlanId: TrainingPlanId
        get() = TrainingPlanId(_trainingPlanId)

    private val _trainingPlan = MutableLiveData<ResultHandler<TrainingPlan>>()
    val trainingPlan: LiveData<ResultHandler<TrainingPlan>> = _trainingPlan

    private val _trainingSessionState = MutableLiveData<TrainingSessionState>()
    val trainingSessionState: LiveData<TrainingSessionState> = _trainingSessionState

    init {
        viewModelScope.launch {
            trainingSessionService.trainingSessionState.collectLatest {
                _trainingSessionState.value = it.toPresentationTrainingSessionState()
            }
        }
    }

    fun fetchTrainingPlan() {
        if (trainingSessionService.isTrainingSessionStarted()) return
        viewModelScope.launch {
            _trainingPlan.value = ResultHandler.Loading
            _trainingPlan.value =
                ResultHandler.Success(dummyTrainingsList.first { it.id == trainingPlanId })
        }
    }

    fun startTrainingSession(trainingPlan: TrainingPlan) {
        trainingSessionService.startTraining(trainingPlan.toDomainTrainingPlan())
    }

    fun completed() {
        trainingSessionService.completed()
    }

    fun skip() {
        trainingSessionService.skip()
    }
}
