package com.lukasz.witkowski.training.planner.exercise.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails

@Composable
fun ExerciseConflictDialog(
    localExerciseDetails: ExerciseDetails,
    serverExerciseDetails: ExerciseDetails,
    onResolveKeepLocal: () -> Unit,
    onResolveAcceptServer: () -> Unit,
    onDismiss: () -> Unit
) {
    val localExercise = localExerciseDetails.exercise
    val serverExercise = serverExerciseDetails.exercise
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Resolve Exercise Conflict")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "A version conflict occurred while updating this exercise.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Local version: ${localExercise.name}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Server version: ${serverExercise.name} (Rev ${serverExercise.metadata.revision})",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onResolveKeepLocal) {
                Text("Keep Local Edit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onResolveAcceptServer) {
                Text("Accept Server Version")
            }
        }
    )
}
