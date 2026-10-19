package com.lukasz.witkowski.training.planner.exercise.presentation.delete

import android.widget.Toast
import androidx.compose.foundation.layout.Box
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
import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.shared.R as SharedR
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.dialogs.AppConfirmationDialog
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppLoadingOverlay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DeleteExerciseScreen(
    viewModel: DeleteExerciseViewModel,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
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
fun DeleteExerciseDialog(
    state: DeleteExerciseUiState,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val current = state) {
        DeleteExerciseUiState.Loading -> {
            AppLoadingOverlay(
                isLoading = true,
                loadingTitle = stringResource(R.string.exercise_details),
                modifier = modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        is DeleteExerciseUiState.LoadedExercise -> {
            AppConfirmationDialog(
                title = stringResource(R.string.delete_exercise_dialog_title, current.exerciseName),
                message = stringResource(R.string.delete_exercise_dialog_text),
                confirmText = stringResource(SharedR.string.delete),
                dismissText = stringResource(SharedR.string.cancel),
                onConfirm = onDelete,
                onDismiss = onCancel,
                isDestructive = true,
                dialogTestTag = "DeleteExerciseDialog",
                titleTestTag = "DeleteExerciseTitle",
                confirmButtonTestTag = "confirmButton",
                dismissButtonTestTag = "dismissButton",
                modifier = modifier
            )
        }

        is DeleteExerciseUiState.Error -> {
            AppConfirmationDialog(
                title = stringResource(R.string.exercise_deletion_failed),
                message = current.message,
                confirmText = stringResource(SharedR.string.delete),
                dismissText = stringResource(SharedR.string.cancel),
                onConfirm = onCancel,
                onDismiss = onCancel,
                isDestructive = false,
                dialogTestTag = "DeleteExerciseDialog",
                titleTestTag = "DeleteExerciseTitle",
                confirmButtonTestTag = "confirmButton",
                dismissButtonTestTag = "dismissButton",
                modifier = modifier
            )
        }
    }
}

@Composable
@Preview
private fun DeleteExerciseDialogPreview() {
    TrainingPlannerTheme2 {
        Surface {
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
