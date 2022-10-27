package com.lukasz.witkowski.training.planner.exercise.presentation

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.exercise.presentation.models.CategoryMapper

class DefaultCategoriesCollection : CategoriesCollection {
    override val allCategories: List<Category>
        get() = ExerciseCategory.values().toList().map { CategoryMapper.toCategory(it) }
}
