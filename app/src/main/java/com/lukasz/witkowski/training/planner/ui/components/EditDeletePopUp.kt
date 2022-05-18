package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.ui.theme.LightGrey
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun EditDeletePopUp(
    modifier: Modifier = Modifier,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {

    Column(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .width(IntrinsicSize.Max)) {
        PopUpButton(Modifier.fillMaxWidth(), text = "Edit", action = onEditClicked)
        Spacer(modifier = Modifier.height(4.dp))
        PopUpButton(
            Modifier.fillMaxWidth(),
            text = "Delete",
            action = onDeleteClicked,
            textColor = Color.Red
        )
    }
}

@Composable
private fun PopUpButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = MaterialTheme.colors.onPrimary,
    action: () -> Unit
) {
    Box(
        modifier = modifier
            .background(color = LightGrey)
            .padding(horizontal = 74.dp, vertical = 32.dp)
            .clickable { action() }
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun EditDeletePopUpPreview() {
    TrainingPlannerTheme {
        EditDeletePopUp(onEditClicked = {}, onDeleteClicked = {})
    }
}
