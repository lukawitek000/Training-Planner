package com.lukasz.witkowski.training.planner.exercise.exercisesList

import com.lukasz.witkowski.training.planner.exercise.application.CategoryService
import com.lukasz.witkowski.training.planner.exercise.models.Category
import com.lukasz.witkowski.training.planner.exercise.models.CategoryMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// TODO I would move it to the Exercise module in presentation layer
class DefaultCategoryController(private val categoryService: CategoryService) : CategoryController {
    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    override val selectedCategories: StateFlow<List<Category>>
        get() = _selectedCategories

    override val filterCategories: List<Category>
        get() = categoryService.getAllCategoriesWithoutNone().map { CategoryMapper.toCategory(it) }

    override fun selectCategory(category: Category) {
        val list = _selectedCategories.value.toMutableList()
        if (!list.remove(category)) {
            list.add(category)
        }
        _selectedCategories.value = list.toList()
    }
}