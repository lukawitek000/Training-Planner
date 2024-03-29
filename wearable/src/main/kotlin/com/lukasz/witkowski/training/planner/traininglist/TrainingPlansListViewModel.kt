package com.lukasz.witkowski.training.planner.traininglist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.dummyTrainingsList
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrainingPlansListViewModel(private val trainingPlanService: TrainingPlanService) :
    ViewModel() {
    private val _trainingPlans = MutableLiveData<ResultHandler<List<TrainingPlan>>>()
    val trainingPlans: LiveData<ResultHandler<List<TrainingPlan>>> = _trainingPlans

    fun getTrainingPlans() {
        viewModelScope.launch {
            try {
                _trainingPlans.value = ResultHandler.Loading
                trainingPlanService.getTrainingPlansFromCategories().collectLatest {
//                _trainingPlans.value = ResultHandler.Success(TrainingPlanMapper.toPresentationTrainingPlans(it))
                    _trainingPlans.value = ResultHandler.Success(dummyTrainingsList)
                }
            } catch (e: Exception) {
                _trainingPlans.value = ResultHandler.Error("Training plan fetch failed", e)
            }
        }
    }
}
