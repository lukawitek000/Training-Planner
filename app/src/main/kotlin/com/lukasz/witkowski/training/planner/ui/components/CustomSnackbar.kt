package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12

@Composable
fun CustomSnackbar(
    modifier: Modifier = Modifier,
    snackbarData: SnackbarData
) {
    Snackbar(
        modifier = modifier.padding(8.dp).padding(top = 24.dp),
        content = {
            SnackbarContent(
                modifier = Modifier.padding(vertical = 4.dp),
                message = snackbarData.message
            )
        },
        action = {
            snackbarData.actionLabel?.let {
                SnackbarAction(performAction = { snackbarData.performAction() }, text = it)
            }
        },
        backgroundColor = LightDark12
    )
}

@Composable
private fun SnackbarContent(
    modifier: Modifier = Modifier,
    message: String
) {
    Text(modifier = modifier, text = message, color = MaterialTheme.colors.primary)
}

@Composable
private fun SnackbarAction(
    modifier: Modifier = Modifier,
    performAction: () -> Unit,
    text: String
) {
    Button(
        modifier = modifier,
        onClick = performAction) {
        Text(text = text)
    }
}