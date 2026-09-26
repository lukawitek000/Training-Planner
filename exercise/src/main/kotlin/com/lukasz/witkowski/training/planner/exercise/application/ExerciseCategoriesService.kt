package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryRepository

class ExerciseCategoriesService(
    private val categoryRepository: ExerciseCategoryRepository
) {
    fun getAllCategories() = categoryRepository.getAll()
}
