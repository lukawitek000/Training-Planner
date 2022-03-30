package com.lukasz.witkowski.shared.models

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
