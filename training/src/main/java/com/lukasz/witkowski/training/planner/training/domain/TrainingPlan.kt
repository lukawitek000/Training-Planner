package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.training.planner.exercise.Category
import java.util.*

data class TrainingPlan(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val exercises: List<TrainingExercise>,
    val isSynchronized: Boolean = false // TODO how to handle synchronization flags
) {

    fun hasCategories(categories: List<com.lukasz.witkowski.training.planner.exercise.Category>): Boolean {
        return getAllCategories().containsAll(categories)
    }

    fun getAllCategories(): List<com.lukasz.witkowski.training.planner.exercise.Category> {
        return exercises.map { exercise -> exercise.category }.filter { category -> !category.isNone() }
    }
}
