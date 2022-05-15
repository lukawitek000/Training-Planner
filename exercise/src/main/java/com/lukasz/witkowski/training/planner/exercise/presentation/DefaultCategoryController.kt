package com.lukasz.witkowski.training.planner.exercise.presentation

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.exercise.presentation.models.CategoryMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// TODO I would move it to the Exercise module in presentation layer, even separate module for category??
class DefaultCategoryController : CategoryController {
    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    override val selectedCategories: StateFlow<List<Category>>
        get() = _selectedCategories

    override val filterCategories: List<Category>
        get() = getAllCategoriesWithoutNone()

    override val allCategories: List<Category>
        get() = ExerciseCategory.values().toList().map { CategoryMapper.toCategory(it) }

    override fun selectCategory(category: Category) {
        val list = _selectedCategories.value.toMutableList()
        if (!list.remove(category)) {
            list.add(category)
        }
        _selectedCategories.value = list.toList()
    }

    private fun getAllCategoriesWithoutNone(): List<Category> {
        return allCategories.filter { !it.isNone() }
    }
}
