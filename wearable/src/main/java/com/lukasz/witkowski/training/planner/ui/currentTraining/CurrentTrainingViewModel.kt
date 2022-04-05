package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.dummyTrainingsList
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrentTrainingViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingPlanService: TrainingPlanService
) : ViewModel() {

    private val _trainingPlan = MutableLiveData<ResultHandler<TrainingPlan>>()
    val trainingPlan: LiveData<ResultHandler<TrainingPlan>> = _trainingPlan

    private var trainingPlanId: TrainingPlanId? = null

    fun fetchTrainingPlan() {
        viewModelScope.launch {
            _trainingPlan.value = ResultHandler.Loading
            _trainingPlan.value = ResultHandler.Success(dummyTrainingsList.first { it.id == trainingPlanId })
//            _trainingWithExercises.value = ResultHandler.Success(trainingRepository.fetchDummyTrainingById(trainingId))
//            _trainingWithExercises.value = ResultHandler.Success(trainingPlanService.fetchTrainingById(trainingId))
        }
    }

    fun setTrainingId(trainingId: String) {
        trainingPlanId = TrainingPlanId(trainingId)
    }
}
