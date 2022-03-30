package com.lukasz.witkowski.training.planner.exercise.exercisesList

import com.lukasz.witkowski.training.planner.exercise.models.Category
import kotlinx.coroutines.flow.StateFlow

interface CategoryController {

    val selectedCategories: StateFlow<List<Category>>

    val filterCategories: List<Category>

    fun selectCategory(category: Category)
}