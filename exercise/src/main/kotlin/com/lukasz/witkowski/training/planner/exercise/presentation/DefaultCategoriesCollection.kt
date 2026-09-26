package com.lukasz.witkowski.training.planner.exercise.presentation

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryLegacy
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toCategory

class DefaultCategoriesCollection : CategoriesCollection {
    override val allCategories: List<Category>
        get() = ExerciseCategoryLegacy.entries.filter { !it.isNone() }.map { it.toCategory() }
}
