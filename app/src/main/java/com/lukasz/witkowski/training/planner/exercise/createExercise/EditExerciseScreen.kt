package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EditExerciseScreen(
    modifier: Modifier,
    viewModel: EditExerciseViewModel,
    onExerciseUpdated: (String) -> Unit
) {
    CreateExerciseScreen(
        modifier = modifier,
        viewModel = viewModel,
        navigateBack = onExerciseUpdated
    )
}