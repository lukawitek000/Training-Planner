package com.lukasz.witkowski.training.planner.exercise.delete

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.components.ConfirmDeleteDialog
import com.lukasz.witkowski.training.planner.ui.components.ConfirmDeleteUiState
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DeleteExerciseScreen(
    viewModel: DeleteExerciseViewModel,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val successMessage = stringResource(R.string.exercise_deleted)
    val failureMessage = stringResource(R.string.exercise_deletion_failed)
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.deletionEvent.collectLatest { event ->
            val text = when (event) {
                DeletionEvent.Failure -> failureMessage
                DeletionEvent.Success -> successMessage
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            onDelete()
        }
    }
    val state by viewModel.state.collectAsState()
    DeleteExerciseDialog(
        state = state,
        onDelete = { viewModel.deleteExercise() },
        onCancel = onCancel,
        modifier = modifier
    )
}

@Composable
private fun DeleteExerciseDialog(
    state: DeleteExerciseUiState,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dialogState = when (val current = state) {
        DeleteExerciseUiState.Loading -> ConfirmDeleteUiState.Loading
        is DeleteExerciseUiState.LoadedExercise -> ConfirmDeleteUiState.Loaded(
            title = stringResource(R.string.delete_exercise_dialog_title, current.exerciseName),
            message = stringResource(R.string.delete_exercise_dialog_text)
        )

        is DeleteExerciseUiState.Error -> ConfirmDeleteUiState.Error(
            title = stringResource(R.string.exercise_deletion_failed),
            message = current.message
        )
    }

    ConfirmDeleteDialog(
        state = dialogState,
        onDelete = onDelete,
        onCancel = onCancel,
        modifier = modifier,
        dialogTestTag = "DeleteExerciseDialog",
        titleTestTag = "DeleteExerciseTitle"
    )
}


@Composable
@Preview
private fun DeleteExerciseDialogPreview() {
    TrainingPlannerTheme {
        Surface() {
            DeleteExerciseDialog(
                state = DeleteExerciseUiState.LoadedExercise(
                    exerciseName = "Push up"
                ),
                onDelete = {},
                onCancel = {},
                modifier = Modifier
            )
        }
    }
}
