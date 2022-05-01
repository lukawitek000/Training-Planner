package com.lukasz.witkowski.training.planner.trainingplans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.dummyTrainingsList
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingsListViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingPlanService: TrainingPlanService
) : ViewModel() {


    private val _trainingPlans = MutableLiveData<ResultHandler<List<TrainingPlan>>>()
    val trainingPlans: LiveData<ResultHandler<List<TrainingPlan>>> = _trainingPlans

    fun getTrainingPlans() {
        viewModelScope.launch {
            _trainingPlans.value = ResultHandler.Loading
            trainingPlanService.getTrainingPlansFromCategories().collectLatest {
//                _trainings.value = ResultHandler.Success(it)
                _trainingPlans.value = ResultHandler.Success(dummyTrainingsList)
            }
        }
    }
}