package com.lukasz.witkowski.training.planner.trainingSession.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun FabTextWithIcon(
    modifier: Modifier = Modifier,
    text: String,
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, textAlign = TextAlign.Center)
            Icon(
                imageVector = imageVector,
                contentDescription = text
            )
        }
    }
}

@Preview
@Composable
fun FabTextWithIcon() {
    TrainingPlannerTheme {
        FabTextWithIcon(
            text = "Skip",
            imageVector = Icons.Filled.SkipNext,
            onClick = {}
        )
    }
}