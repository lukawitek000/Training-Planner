package com.lukasz.witkowski.training.planner.exercise.presentation.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId

@Composable
fun EditExerciseScreen(
    viewModel: ExerciseEditorViewModel,
    navigateToDetails: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier
) {
    CreateExerciseScreen(
        viewModel = viewModel,
        navigateToDetails = navigateToDetails,
        modifier = modifier
    )
}
