package com.lukasz.witkowski.training.planner.shared.ui.chips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Visual style variants for [AppCategoryPill].
 */
enum class AppCategoryPillStyle {
    /** High-contrast filled pill: `primaryContainer` background with `onPrimary` text. */
    Filled,

    /** Tonal pill: `primaryContainer.copy(alpha = 0.15f)` background with `primary` text. */
    Tonal
}

/**
 * Compact category pill composable styled according to Stitch design specification.
 *
 * Spec Highlights:
 * - Filled: `primaryContainer` background with `onPrimary` bold uppercase `labelSmall` text.
 * - Tonal: `primaryContainer.copy(alpha = 0.15f)` background with `primary` bold uppercase `labelSmall` text.
 */
@Composable
fun AppCategoryPill(
    text: String,
    modifier: Modifier = Modifier,
    style: AppCategoryPillStyle = AppCategoryPillStyle.Filled,
    uppercase: Boolean = true
) {
    val containerColor =
        when (style) {
            AppCategoryPillStyle.Filled -> MaterialTheme.colorScheme.primaryContainer
            AppCategoryPillStyle.Tonal -> MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.15f
            )
        }

    val contentColor =
        when (style) {
            AppCategoryPillStyle.Filled -> MaterialTheme.colorScheme.onPrimary
            AppCategoryPillStyle.Tonal -> MaterialTheme.colorScheme.primary
        }

    val borderStroke =
        when (style) {
            AppCategoryPillStyle.Filled -> null
            AppCategoryPillStyle.Tonal ->
                BorderStroke(
                    width = Dimens2.thinBorder,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
        }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke
    ) {
        Box(
            modifier =
            Modifier.padding(
                horizontal = Dimens2.badgeHorizontalPadding,
                vertical = Dimens2.badgeVerticalPadding
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (uppercase) text.uppercase() else text,
                style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )
        }
    }
}

@AppPreview
@Composable
private fun AppCategoryPillPreview() {
    TrainingPlannerTheme2 {
        Row(
            modifier = Modifier.padding(Dimens2.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            AppCategoryPill(
                text = "Chest",
                style = AppCategoryPillStyle.Filled
            )
            AppCategoryPill(
                text = "Shoulders",
                style = AppCategoryPillStyle.Filled
            )
            AppCategoryPill(
                text = "Triceps",
                style = AppCategoryPillStyle.Tonal
            )
        }
    }
}
