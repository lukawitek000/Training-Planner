package com.lukasz.witkowski.training.planner.session.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Quick feedback chips for rating set difficulty during rest intervals.
 */
@Composable
fun <T> AppFeedbackChips(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            val backgroundColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
            val textColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val borderColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(
                        width = if (isSelected) Dimens.mediumBorder else Dimens.thinBorder,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable(
                        role = Role.RadioButton,
                        onClick = { onOptionSelected(option) }
                    )
                    .padding(vertical = Dimens.spaceSm, horizontal = Dimens.spaceXs),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelProvider(option),
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    maxLines = 1
                )
            }
        }
    }
}

@AppPreview
@Composable
private fun AppFeedbackChipsPreview() {
    val options = listOf("Easy", "Good", "Hard", "Failed")
    TrainingPlannerTheme {
        AppFeedbackChips(
            options = options,
            selectedOption = "Good",
            onOptionSelected = {},
            labelProvider = { it },
            modifier = Modifier.padding(Dimens.spaceMd)
        )
    }
}
