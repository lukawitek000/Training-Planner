package com.lukasz.witkowski.training.planner.training.trainingsList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoryController
import com.lukasz.witkowski.training.planner.exercise.presentation.models.CategoryMapper
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import com.lukasz.witkowski.training.planner.training.presentation.mappers.TrainingPlanMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrainingsListViewModel(
    private val trainingPlanService: TrainingPlanService,
    categoryController: CategoryController
) : ViewModel(), CategoryController by categoryController {


    private val _trainingPlans = MutableStateFlow<List<TrainingPlan>>(emptyList())
    val trainingPlans: StateFlow<List<TrainingPlan>>
        get() = _trainingPlans

    init {
        fetchExercises()
        observeSelectedCategories()
    }

    private fun observeSelectedCategories() {
        viewModelScope.launch {
            selectedCategories.collectLatest {
                fetchExercises()
            }
        }
    }

    private fun fetchExercises() {
        viewModelScope.launch {
            val categories = selectedCategories.value.map { CategoryMapper.toExerciseCategory(it) }
            trainingPlanService.getTrainingPlansFromCategories(categories).collectLatest {
                _trainingPlans.emit(TrainingPlanMapper.toPresentationTrainingPlans(it))
            }
        }
    }

    fun sendTrainingPlan(id: String) {
        viewModelScope.launch {
            _trainingPlans.value.firstOrNull { it.id.toString() == id }?.let {
                trainingPlanService.sendTrainingPlan(TrainingPlanMapper.toDomainTrainingPlan(it))
            }
        }
    }
}
