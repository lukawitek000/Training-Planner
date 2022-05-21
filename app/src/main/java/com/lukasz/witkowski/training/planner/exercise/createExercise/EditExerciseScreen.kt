package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.SnackbarState

@Composable
fun EditExerciseScreen(
    modifier: Modifier,
    viewModel: EditExerciseViewModel,
    snackbarState: SnackbarState,
    navigateUp: () -> Unit
) {
    val text = stringResource(id = R.string.exercise_updated)
    val failMessage = stringResource(id = R.string.exercise_update_failed)
    CreateExerciseScreen(
        modifier = modifier,
        viewModel = viewModel,
        snackbarState = snackbarState,
        navigateUp = navigateUp,
        successMessage = text,
        failMessage = failMessage
    )
}