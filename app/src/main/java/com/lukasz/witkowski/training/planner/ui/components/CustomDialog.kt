package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DialogContainer(
    modifier: Modifier = Modifier,
    closeDialog: () -> Unit,
    saveData: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = { closeDialog() })
    {
        Scaffold(
            modifier = modifier
                .height(600.dp)
                .clip(MaterialTheme.shapes.medium),
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    saveData()
                    closeDialog()
                }) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Done"
                    )
                }
            },
            backgroundColor = Color.Gray
        )
        {
            content()
        }
    }
}