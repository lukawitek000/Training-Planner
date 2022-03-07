package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrentTrainingViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingPlanService: TrainingPlanService
) : ViewModel() {

    private val _trainingWithExercises = MutableLiveData<ResultHandler<TrainingPlan>>()
    val trainingWithExercises: LiveData<ResultHandler<TrainingPlan>> = _trainingWithExercises

    fun fetchTraining(trainingId: Long) {
        viewModelScope.launch {
            _trainingWithExercises.value = ResultHandler.Loading
//            _trainingWithExercises.value = ResultHandler.Success(trainingRepository.fetchDummyTrainingById(trainingId))
//            _trainingWithExercises.value = ResultHandler.Success(trainingPlanService.fetchTrainingById(trainingId))
        }
    }
}
