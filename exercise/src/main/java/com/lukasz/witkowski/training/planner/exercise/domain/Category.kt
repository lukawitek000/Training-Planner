package com.lukasz.witkowski.training.planner.exercise.domain

sealed class Category(val name: String) {

    object None : Category("")
    object Legs : Category("Legs")
    object Shoulders : Category("Shoulders")
    object Biceps : Category("Biceps")
    object Triceps : Category("Triceps")
    object Cardio : Category("Cardio")
    object Back : Category("Back")
    object Abs : Category("Abs")
    object Stretching : Category("Stretching")
}


val allCategories = Category::class.sealedSubclasses.map { it.objectInstance as Category }
val categoriesWithoutNone = allCategories.filter { it.name.isNotEmpty() }
