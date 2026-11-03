package com.lukasz.witkowski.training.planner.session.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard

/**
 * Numeric stepper component for adjusting exercise repetitions and loads.
 */
@Composable
fun AppStepper(
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    step: Double = 1.0,
    minValue: Double = 0.0,
    maxValue: Double = Double.MAX_VALUE,
    label: String? = null,
    formatValue: (Double) -> String = {
        if (it % 1.0 == 0.0) {
            it.toInt().toString()
        } else {
            "%.1f".format(it)
        }
    },
    enabled: Boolean = true
) {
    AppCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens2.spaceMd, vertical = Dimens2.spaceSm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val canDecrement = enabled && value > minValue
            val canIncrement = enabled && value < maxValue

            StepperActionButton(
                icon = Icons.Default.Remove,
                contentDescription = stringResource(R.string.stepper_decrease_cd),
                enabled = canDecrement,
                onClick = {
                    val newValue = (value - step).coerceAtLeast(minValue)
                    onValueChange(newValue)
                }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formatValue(value),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            StepperActionButton(
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.stepper_increase_cd),
                enabled = canIncrement,
                onClick = {
                    val newValue = (value + step).coerceAtMost(maxValue)
                    onValueChange(newValue)
                }
            )
        }
    }
}

/**
 * Overload for integer values (e.g. repetitions).
 */
@Composable
fun AppIntStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 1,
    minValue: Int = 0,
    maxValue: Int = Int.MAX_VALUE,
    label: String? = null,
    enabled: Boolean = true
) {
    AppStepper(
        value = value.toDouble(),
        onValueChange = { onValueChange(it.toInt()) },
        modifier = modifier,
        step = step.toDouble(),
        minValue = minValue.toDouble(),
        maxValue = maxValue.toDouble(),
        label = label,
        formatValue = { it.toInt().toString() },
        enabled = enabled
    )
}

@Composable
private fun StepperActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (enabled) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val iconColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = modifier
            .size(Dimens2.stepperButtonSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = Dimens2.thinBorder,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(Dimens2.iconSize24)
        )
    }
}

@AppPreview
@Composable
private fun AppStepperPreview() {
    TrainingPlannerTheme2 {
        Column(
            modifier = Modifier.padding(Dimens2.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd)
        ) {
            AppIntStepper(
                value = 10,
                onValueChange = {},
                label = "reps"
            )
            AppStepper(
                value = 82.5,
                onValueChange = {},
                step = 2.5,
                label = "kg"
            )
        }
    }
}
