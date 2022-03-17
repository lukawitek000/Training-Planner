package com.lukasz.witkowski.training.planner.ui

import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseCategoryFilteredListViewModel() : ViewModel() {

    private val _selectedCategories = MutableStateFlow<List<ExerciseCategory>>(emptyList())
    val selectedCategories: StateFlow<List<ExerciseCategory>> = _selectedCategories

    fun selectCategory(category: ExerciseCategory) {
        val list = _selectedCategories.value.toMutableList()
        if (list.contains(category)) {
            list.remove(category)
        } else {
            list.add(category)
        }
        _selectedCategories.value = list.toList()
        fetchData()
    }

    abstract fun fetchData()

}