package com.lukasz.witkowski.training.planner.exercise

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher

val exerciseItemMatcher = SemanticsMatcher("TestTag contains 'ExerciseItem-'") {
    it.config.getOrNull(SemanticsProperties.TestTag)
        ?.contains("ExerciseItem-") == true
}

fun givenExerciseItemTag(exerciseName: String) = "ExerciseItem-$exerciseName"
