package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.R

@Composable
fun EditExerciseScreen(
    modifier: Modifier,
    viewModel: EditExerciseViewModel,
    showSnackbar: (String) -> Unit,
    navigateUp: () -> Unit
) {
    val text = stringResource(id = R.string.exercise_updated)
    val failMessage = stringResource(id = R.string.exercise_update_failed)
    CreateExerciseScreen(
        modifier = modifier,
        viewModel = viewModel,
        showSnackbar = showSnackbar,
        navigateUp = navigateUp,
        successMessage = text,
        failMessage = failMessage
    )
}