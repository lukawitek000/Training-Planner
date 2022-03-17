package com.lukasz.witkowski.training.planner.training.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class TrainingsListViewModel @Inject constructor(
    private val trainingPlanService: TrainingPlanService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // TODO the same filtering as in the exercises
    private val _selectedCategories = MutableStateFlow<List<ExerciseCategory>>(emptyList())
    val selectedCategories: StateFlow<List<ExerciseCategory>>
        get() = _selectedCategories

    private val _trainingPlans = MutableStateFlow<List<TrainingPlan>>(emptyList())
    val trainingPlans: StateFlow<List<TrainingPlan>>
        get() = _trainingPlans

    init {
        fetchTrainingPlans()
    }

    private fun fetchTrainingPlans() {
        trainingPlanService.getAllTrainingPlans(selectedCategories.value)
            .onEach { _trainingPlans.emit(it) }
            .launchIn(viewModelScope)
    }

    fun selectCategory(category: ExerciseCategory) {
        val list = _selectedCategories.value.toMutableList()
        if (!list.remove(category)) {
            list.add(category)
        }
        _selectedCategories.value = list.toList()
        fetchTrainingPlans()
    }
}
