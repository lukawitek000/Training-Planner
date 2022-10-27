package com.lukasz.witkowski.training.planner.exercise.presentation

import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category


// Without his interface I violate Interface Segregation rule in the [CreateExerciseViewModel]
interface CategoriesCollection {
    val allCategories: List<Category>
    val categoriesWithoutNone: List<Category>
        get() = allCategories.filter { !it.isNone() }
}
