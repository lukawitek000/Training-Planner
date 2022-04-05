package com.lukasz.witkowski.training.planner.exercise.presentation

import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import kotlinx.coroutines.flow.StateFlow

// TODO I would move it to the Exercise module in presentation layer
interface CategoryController {

    val selectedCategories: StateFlow<List<Category>>

    val filterCategories: List<Category>

    fun selectCategory(category: Category)
}