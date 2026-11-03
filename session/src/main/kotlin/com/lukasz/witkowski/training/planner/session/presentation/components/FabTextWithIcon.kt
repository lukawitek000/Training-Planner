package com.lukasz.witkowski.training.planner.session.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

@Composable
fun FabTextWithIcon(
    modifier: Modifier = Modifier,
    text: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        containerColor = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens2.spaceMd),
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

@AppPreview
@Composable
private fun FabTextWithIconPreview() {
    TrainingPlannerTheme2 {
        FabTextWithIcon(
            text = "Skip",
            imageVector = Icons.Filled.SkipNext,
            onClick = {}
        )
    }
}
