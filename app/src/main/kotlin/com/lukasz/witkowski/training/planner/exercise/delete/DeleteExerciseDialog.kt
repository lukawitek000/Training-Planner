package com.lukasz.witkowski.training.planner.exercise.delete

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.components.OverlayContent
import com.lukasz.witkowski.training.planner.ui.components.OverlayLoading
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
    when (state) {
        DeleteExerciseUiState.Loading -> OverlayLoading(modifier.fillMaxSize()) { }
        is DeleteExerciseUiState.LoadedExercise -> DeleteExerciseDialog(
            exerciseName = (state as DeleteExerciseUiState.LoadedExercise).exerciseName,
            onDelete = {
                viewModel.deleteExercise()
            },
            onCancel = onCancel,
            modifier = modifier
        )
    }
}


@Composable
private fun DeleteExerciseDialog(
    exerciseName: String,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onCancel,
        title = {
            Text(
                text = stringResource(R.string.delete_exercise_dialog_title, exerciseName),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_exercise_dialog_text),
                textAlign = TextAlign.Center
            )
        },
        dismissButton = {
            Button(
                onClick = onCancel,
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(R.string.delete))
            }
        },
    )
}

@Composable
@Preview
private fun DeleteExerciseDialogPreview() {
    TrainingPlannerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DeleteExerciseDialog(
                onDelete = {},
                onCancel = {},
                exerciseName = "Push ups",
                modifier = Modifier
            )
        }
    }
}
