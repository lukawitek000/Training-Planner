package com.lukasz.witkowski.training.planner.exercise.presentation

import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import kotlinx.coroutines.flow.StateFlow

interface CategoryController : CategoriesCollection {
    val selectedCategories: StateFlow<List<Category>>
    fun selectCategory(category: Category)
}
