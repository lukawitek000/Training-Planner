package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.SnackbarState
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId

@Composable
fun EditExerciseScreen(
    viewModel: ExerciseEditorViewModel,
    navigateToDetails: (ExerciseId, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    CreateExerciseScreen(
        viewModel = viewModel,
        navigateToDetails = navigateToDetails,
        modifier = modifier
    )
}
