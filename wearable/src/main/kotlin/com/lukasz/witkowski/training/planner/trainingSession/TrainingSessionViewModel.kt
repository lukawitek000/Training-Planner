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
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionStateMapper
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.mappers.TrainingPlanMapper
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingSessionViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingPlanService: TrainingPlanService,
    private val trainingSessionService: TrainingSessionService
) : ViewModel() {

    private val _trainingPlanId =
        savedStateHandle.get<String>(StartTrainingActivity.TRAINING_ID_KEY)
            ?: throw IllegalStateException("Missing training id")
    val trainingPlanId: TrainingPlanId
        get() = TrainingPlanId(_trainingPlanId)

    private val _trainingPlan = MutableLiveData<ResultHandler<TrainingPlan>>()
    val trainingPlan: LiveData<ResultHandler<TrainingPlan>> = _trainingPlan

    private val _trainingSessionState = MutableLiveData<TrainingSessionState>()
    val trainingSessionState: LiveData<TrainingSessionState> = _trainingSessionState

    private val _currentExercise = MutableLiveData<TrainingExercise>()
    val currentExercise: LiveData<TrainingExercise> = _currentExercise

    fun fetchTrainingPlan() {
        viewModelScope.launch {
            _trainingPlan.value = ResultHandler.Loading
            _trainingPlan.value = ResultHandler.Success(dummyTrainingsList.first { it.id == trainingPlanId })
        }
    }

    fun startTrainingSession(trainingPlan: TrainingPlan) {
        val state = trainingSessionService.startTraining(TrainingPlanMapper.toDomainTrainingPlan(trainingPlan))
        _trainingSessionState.value = TrainingSessionStateMapper.toPresentation(state)
    }

    fun setCurrentTrainingExercise(trainingExercise: TrainingExercise) {
        _currentExercise.value = trainingExercise
    }
}
