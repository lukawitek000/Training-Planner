package com.lukasz.witkowski.training.planner.training

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.Category
import com.lukasz.witkowski.training.planner.exercise.CategoryMapper
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
    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories: StateFlow<List<Category>>
        get() = _selectedCategories

    private val _trainingPlans = MutableStateFlow<List<TrainingPlan>>(emptyList())
    val trainingPlans: StateFlow<List<TrainingPlan>>
        get() = _trainingPlans

    init {
        fetchTrainingPlans()
    }

    private fun fetchTrainingPlans() {
        val categories = selectedCategories.value.map { CategoryMapper.toDomainCategory(it) }
        trainingPlanService.getAllTrainingPlans(categories = categories)
            .onEach { _trainingPlans.emit(it) }
            .launchIn(viewModelScope)
    }

    fun selectCategory(category: com.lukasz.witkowski.training.planner.exercise.Category) {
        val list = _selectedCategories.value.toMutableList()
        if (!list.remove(category)) {
            list.add(category)
        }
        _selectedCategories.value = list.toList()
        fetchTrainingPlans()
    }
}
