package com.lukasz.witkowski.training.planner.shared.ui.card

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Divider composable intended for in-card content separation according to design specification.
 *
 * Spec Highlights:
 * - Height / Thickness: 1dp [Dimens2.thinBorder]
 * - Color: surfaceContainerHigh
 * - Horizontal Margins: 16dp [Dimens2.spaceMd]
 */
@Composable
fun AppCardDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    thickness: Dp = Dimens2.thinBorder,
    horizontalMargin: Dp = Dimens2.spaceMd,
) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = horizontalMargin),
        thickness = thickness,
        color = color,
    )
}

@AppPreview
@Composable
private fun AppCardDividerPreview() {
    TrainingPlannerTheme2 {
        AppCardDivider()
    }
}
