package com.lukasz.witkowski.training.planner.shared.ui.badges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.Success
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.theme.Warning
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Status pill badge with optional [Dimens.statusDotSize] dot or leading icon.
 *
 * Spec Highlights:
 * - Container: `toneColor.copy(alpha = 0.15f)`
 * - Content: `toneColor`
 * - Border: `toneColor.copy(alpha = 0.3f)`
 * - Text: `labelSmall` uppercase bold
 */
@Composable
fun AppStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    toneColor: Color = MaterialTheme.colorScheme.primaryContainer,
    showDot: Boolean = true,
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String? = null,
    uppercase: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = toneColor.copy(alpha = 0.15f),
        contentColor = toneColor,
        border =
        BorderStroke(
            width = Dimens.thinBorder,
            color = toneColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier =
            Modifier.padding(
                horizontal = Dimens.badgeHorizontalPadding,
                vertical = Dimens.badgeVerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = leadingIconContentDescription,
                    tint = toneColor,
                    modifier = Modifier.size(Dimens.iconSize13)
                )
                Spacer(modifier = Modifier.width(Dimens.spaceXs))
            } else if (showDot) {
                Box(
                    modifier =
                    Modifier
                        .size(Dimens.statusDotSize)
                        .background(color = toneColor, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(Dimens.spaceXs))
            }
            Text(
                text = if (uppercase) text.uppercase() else text,
                style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = toneColor
            )
        }
    }
}

@AppPreview
@Composable
private fun AppStatusBadgePreview() {
    TrainingPlannerTheme {
        Column(
            modifier = Modifier.padding(Dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            AppStatusBadge(
                text = "Synced",
                toneColor = Success,
                leadingIcon = Icons.Default.CloudDone
            )
            AppStatusBadge(
                text = "Conflict",
                toneColor = Warning,
                leadingIcon = Icons.Default.SyncProblem
            )
            AppStatusBadge(
                text = "Sync Error",
                toneColor = MaterialTheme.colorScheme.error,
                showDot = true
            )
            AppStatusBadge(
                text = "Local Only",
                toneColor = MaterialTheme.colorScheme.secondary,
                showDot = true
            )
        }
    }
}
