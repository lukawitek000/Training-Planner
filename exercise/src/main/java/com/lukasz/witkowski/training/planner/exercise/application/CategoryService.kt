package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory

// TODO is it a good way to take categories, in which layer should it belong
// Probably it should have different name??
class CategoryService {

    fun getAllCategories(): List<ExerciseCategory> {
        return ExerciseCategory.values().toList()
    }

    fun getAllCategoriesWithoutNone(): List<ExerciseCategory> {
        return getAllCategories().filter { it != ExerciseCategory.NONE }
    }
}
