package com.lukasz.witkowski.training.planner.shared.ui.inputField

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Form field label composable styled according to design specification.
 */
@Composable
fun AppInputLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = MaterialTheme.typography.labelMedium,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
    )
}

@AppPreview
@Composable
private fun AppInputLabelPreview() {
    TrainingPlannerTheme2 {
        AppInputLabel(text = "Email Address")
    }
}
