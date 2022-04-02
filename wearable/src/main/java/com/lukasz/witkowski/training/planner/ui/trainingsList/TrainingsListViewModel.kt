package com.lukasz.witkowski.training.planner.ui.trainingsList

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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingsListViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trainingPlanService: TrainingPlanService
) : ViewModel() {


    private val _trainings = MutableLiveData<ResultHandler<List<TrainingPlan>>>()
    val trainings: LiveData<ResultHandler<List<TrainingPlan>>> = _trainings

    fun getTrainingsWithExercises() {
        viewModelScope.launch {
            _trainings.value = ResultHandler.Loading
            trainingPlanService.getTrainingPlansFromCategories(emptyList()).collect {
//                _trainings.value = ResultHandler.Success(it)
                _trainings.value = ResultHandler.Success(dummyTrainingsList)
            }
        }
    }
}