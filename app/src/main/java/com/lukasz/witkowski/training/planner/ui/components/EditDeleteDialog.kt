package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.ui.theme.LightGrey
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun EditDeleteDialog(
    modifier: Modifier = Modifier,
    text: String,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
        },
        buttons = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PopUpButton(text = "Edit", action = {
                    onEditClicked()
                    onDismissRequest()
                })
                PopUpButton(
                    text = "Delete",
                    action = {
                        onDeleteClicked()
                        onDismissRequest()
                    },
                    textColor = Color.Red
                )
            }
        }
    )
}

@Composable
private fun PopUpButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = MaterialTheme.colors.onPrimary,
    action: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = action,
        colors = ButtonDefaults.buttonColors(backgroundColor = LightGrey),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Text(
            text = text,
            color = textColor
        )
    }
}

@Preview
@Composable
fun EditDeletePopUpPreview() {
    TrainingPlannerTheme {
        EditDeleteDialog(
            onEditClicked = {},
            onDeleteClicked = {},
            onDismissRequest = {},
            text = "Test title "
        )
    }
}
