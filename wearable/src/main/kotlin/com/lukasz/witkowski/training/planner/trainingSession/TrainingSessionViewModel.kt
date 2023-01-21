package com.lukasz.witkowski.training.planner.trainingSession

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.dummyTrainingsList
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingSessionViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingPlanService: TrainingPlanService
) : ViewModel() {

    private val _trainingPlanId =
        savedStateHandle.get<String>(StartTrainingActivity.TRAINING_ID_KEY)
            ?: throw IllegalStateException("Missing training id")
    val trainingPlanId: TrainingPlanId
        get() = TrainingPlanId(_trainingPlanId)

    private val _trainingPlan = MutableLiveData<ResultHandler<TrainingPlan>>()
    val trainingPlan: LiveData<ResultHandler<TrainingPlan>> = _trainingPlan


    fun fetchTrainingPlan() {
        viewModelScope.launch {
            _trainingPlan.value = ResultHandler.Loading
            _trainingPlan.value = ResultHandler.Success(dummyTrainingsList.first { it.id == trainingPlanId })
        }
    }
}
