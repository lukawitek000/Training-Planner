package com.lukasz.witkowski.training.planner.ui

import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.training.planner.exercise.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseCategoryFilteredListViewModel() : ViewModel() {

    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories: StateFlow<List<Category>> = _selectedCategories

    fun selectCategory(category: Category) {
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