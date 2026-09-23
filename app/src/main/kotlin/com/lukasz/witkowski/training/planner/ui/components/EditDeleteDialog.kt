package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        dismissButton = {
            PopUpButton(
                text = "Delete",
                action = onDeleteClicked,
                textColor = Color.Red
            )
        },
        confirmButton = {
            PopUpButton(text = "Edit", action = onEditClicked)

        },
    )
}

@Composable
private fun PopUpButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    action: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = action,
        colors = ButtonDefaults.buttonColors(containerColor = LightGrey),
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
