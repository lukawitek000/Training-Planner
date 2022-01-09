package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12
import com.lukasz.witkowski.training.planner.ui.theme.LightDark5

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
        Surface(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium),
            color = LightDark5
        )
        {
            Column() {
                content()
                FloatingActionButton(
                    modifier = Modifier.align(Alignment.End).padding(8.dp),
                    onClick = {
                    saveData()
                    closeDialog()
                }) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Done"
                    )
                }
            }

        }
    }
}