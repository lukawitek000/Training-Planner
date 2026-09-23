package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomSnackbar(
    modifier: Modifier = Modifier,
    snackbarData: SnackbarData
) {
    Snackbar(
        modifier = modifier
            .padding(8.dp)
            .padding(top = 24.dp),
        content = {
            SnackbarContent(
                modifier = Modifier.padding(vertical = 4.dp),
                message = snackbarData.visuals.message
            )
        },
        action = {
            snackbarData.visuals.actionLabel?.let {
                SnackbarAction(performAction = { snackbarData.performAction() }, text = it)
            }
        },
    )
}

@Composable
private fun SnackbarContent(
    modifier: Modifier = Modifier,
    message: String
) {
    Text(modifier = modifier, text = message, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun SnackbarAction(
    modifier: Modifier = Modifier,
    performAction: () -> Unit,
    text: String
) {
    Button(
        modifier = modifier,
        onClick = performAction
    ) {
        Text(text = text)
    }
}