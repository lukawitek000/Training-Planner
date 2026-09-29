package com.lukasz.witkowski.training.planner.exercise.presentation

import com.lukasz.witkowski.training.planner.exercise.application.ExerciseCategoriesService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

data class FilterCategory(
    val category: ExerciseCategory,
    val isSelected: Boolean = false,
)

interface CategoryController2 {
    val filterCategories: Flow<List<FilterCategory>>
    fun toggleCategory(category: ExerciseCategory)
    fun selectCategories(categories: Set<ExerciseCategory>)
}

class DefaultCategoryController2(
    categoryService: ExerciseCategoriesService,
) : CategoryController2 {
    private val selectedCategories = MutableStateFlow(setOf<ExerciseCategory>())
    private val allCategories: Flow<List<ExerciseCategory>> = categoryService.getAllCategories()
    override val filterCategories: Flow<List<FilterCategory>> =
        combine(allCategories, selectedCategories) { all, selected ->
            all.map {
                FilterCategory(
                    category = it,
                    isSelected = selected.contains(it)
                )
            }.sortedByDescending { it.isSelected }
        }

    override fun toggleCategory(category: ExerciseCategory) {
        selectedCategories.update { set ->
            if (set.contains(category)) {
                set - category
            } else {
                set + category
            }
        }
    }

    override fun selectCategories(categories: Set<ExerciseCategory>) {
        selectedCategories.value = categories
    }
}