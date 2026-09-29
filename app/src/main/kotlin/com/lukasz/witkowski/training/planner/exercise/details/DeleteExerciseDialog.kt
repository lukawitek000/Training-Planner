package com.lukasz.witkowski.training.planner.exercise.details

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun DeleteExerciseDialog(
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
