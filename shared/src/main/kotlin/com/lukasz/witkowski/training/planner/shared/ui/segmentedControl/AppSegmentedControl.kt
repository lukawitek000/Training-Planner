package com.lukasz.witkowski.training.planner.shared.ui.segmentedControl

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2

/**
 * Item specification for [AppSegmentedControl].
 */
data class AppSegmentedControlItem<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
    val iconContentDescription: String? = null,
)

/**
 * Segmented control / mode switch composable styled according to design specification.
 *
 * Spec Highlights:
 * - Outer container: surfaceContainerLowest background, rounded-xl shape, 1dp surfaceContainerHigh (40% alpha) border, 4dp padding.
 * - Selected Segment: primaryContainer background, onPrimary text color, bold titleSmall typography, 2dp shadow-sm elevation.
 * - Unselected Segment: Transparent background, secondary text color, titleSmall typography.
 */
@Composable
fun <T> AppSegmentedControl(
    items: List<AppSegmentedControlItem<T>>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val containerShape = MaterialTheme.shapes.medium
    val borderStroke = BorderStroke(
        width = Dimens2.thinBorder,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(border = borderStroke, shape = containerShape),
        shape = containerShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens2.spaceXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val isSelected = item.value == selectedItem

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "AppSegmentedControlBackgroundColor",
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "AppSegmentedControlTextColor",
                )

                val segmentShape = MaterialTheme.shapes.small
                val segmentElevation = if (isSelected) Dimens2.shadowSm else Dimens2.zeroDp

                Surface(
                    onClick = { if (enabled) onItemSelected(item.value) },
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    shape = segmentShape,
                    color = backgroundColor,
                    shadowElevation = segmentElevation,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens2.segmentVerticalPadding),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.iconContentDescription,
                                tint = textColor,
                                modifier = Modifier.size(Dimens2.iconSize18),
                            )
                            Spacer(modifier = Modifier.width(Dimens2.spaceGapLabelInput))
                        }
                        Text(
                            text = item.label,
                            color = textColor,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private enum class AuthModePreview { SIGN_IN, SIGN_UP }

@Preview(showBackground = true, backgroundColor = 0xFF1A120A)
@Composable
private fun AppSegmentedControlPreview() {
    var selectedMode by remember { mutableStateOf(AuthModePreview.SIGN_UP) }

    val items = listOf(
        AppSegmentedControlItem(
            value = AuthModePreview.SIGN_IN,
            label = "Sign In",
            icon = Icons.AutoMirrored.Filled.Login,
        ),
        AppSegmentedControlItem(
            value = AuthModePreview.SIGN_UP,
            label = "Sign Up",
            icon = Icons.Default.PersonAdd,
        ),
    )

    TrainingPlannerTheme2 {
        Box(modifier = Modifier.padding(Dimens2.spaceMd)) {
            AppSegmentedControl(
                items = items,
                selectedItem = selectedMode,
                onItemSelected = { selectedMode = it },
            )
        }
    }
}
