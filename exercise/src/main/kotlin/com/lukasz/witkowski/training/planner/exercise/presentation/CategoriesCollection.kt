package com.lukasz.witkowski.training.planner.exercise.presentation

import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category

interface CategoriesCollection {
    val allCategories: List<Category>
    val categoriesWithoutNone: List<Category>
        get() = allCategories.filter { !it.isNone() }
}
