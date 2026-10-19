package com.lukasz.witkowski.training.planner.shared.ui.chips

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Selectable filter pill composable styled according to Stitch design specification.
 *
 * Spec Highlights:
 * - Shape: [CircleShape]
 * - Height: [Dimens2.chipHeightMedium] (36dp)
 * - Selected state: [MaterialTheme.colorScheme.primaryContainer] background, [MaterialTheme.colorScheme.onPrimary] bold text, optional leading check icon, [Dimens2.shadowSm] elevation.
 * - Unselected state: [MaterialTheme.colorScheme.surfaceContainerHigh] background, [MaterialTheme.colorScheme.onSurfaceVariant] text, subtle [MaterialTheme.colorScheme.outlineVariant] border.
 */
@Composable
fun AppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showCheckIcon: Boolean = false,
    leadingIcon: ImageVector? = if (selected && showCheckIcon) Icons.Default.Check else null,
    leadingIconContentDescription: String? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue =
        if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(durationMillis = 150),
        label = "AppFilterChipBackgroundColor"
    )

    val contentColor by animateColorAsState(
        targetValue =
        if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 150),
        label = "AppFilterChipContentColor"
    )

    val borderStroke =
        if (selected) {
            null
        } else {
            BorderStroke(
                width = Dimens2.thinBorder,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }

    Surface(
        onClick = onClick,
        modifier =
        modifier
            .height(Dimens2.chipHeightMedium)
            .semantics { this.selected = selected },
        enabled = enabled,
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor,
        border = borderStroke,
        shadowElevation = if (selected) Dimens2.shadowSm else Dimens2.zeroDp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens2.inputHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = leadingIconContentDescription,
                    tint = contentColor,
                    modifier = Modifier.size(Dimens2.iconSize14)
                )
                Spacer(modifier = Modifier.width(Dimens2.spaceXs))
            }
            Text(
                text = label.uppercase(),
                style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

@AppPreview
@Composable
private fun AppFilterChipPreview() {
    TrainingPlannerTheme2 {
        Row(
            modifier = Modifier.padding(Dimens2.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            AppFilterChip(
                label = "All",
                selected = true,
                onClick = {}
            )
            AppFilterChip(
                label = "Chest",
                selected = true,
                showCheckIcon = true,
                onClick = {}
            )
            AppFilterChip(
                label = "Shoulders",
                selected = false,
                onClick = {}
            )
            AppFilterChip(
                label = "Legs",
                selected = false,
                onClick = {}
            )
        }
    }
}
