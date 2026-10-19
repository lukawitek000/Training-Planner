package com.lukasz.witkowski.training.planner.shared.ui.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.appIcon.AppIconBox

/**
 * Centered empty state composable styled according to Stitch design specification.
 *
 * Spec Highlights:
 * - Icon: [AppIconBox] with [Icons.Default.FitnessCenter] (or custom [icon])
 * - Title: [MaterialTheme.typography.titleMedium] bold in [MaterialTheme.colorScheme.onSurface]
 * - Subtitle: [MaterialTheme.typography.bodySmall] in [MaterialTheme.colorScheme.onSurfaceVariant], max width [Dimens2.maxSubtitleWidth]
 * - Optional CTA button: Rounded pill button in [MaterialTheme.colorScheme.surfaceContainerHighest] with [MaterialTheme.colorScheme.primaryContainer] text
 */
@Composable
fun AppEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector = Icons.Default.FitnessCenter,
    iconContentDescription: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens2.spaceLg,
                vertical = Dimens2.spaceXl
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppIconBox(
            mainIcon = icon,
            mainIconContentDescription = iconContentDescription
        )

        Spacer(modifier = Modifier.height(Dimens2.spaceGapIconTextMedium))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(Dimens2.spaceXs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = Dimens2.maxSubtitleWidth)
            )
        }

        if (!actionLabel.isNullOrBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.height(Dimens2.spaceMd))
            Surface(
                onClick = onActionClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier =
                    Modifier.padding(
                        horizontal = Dimens2.spaceMd,
                        vertical = Dimens2.spaceSm
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@AppPreview
@Composable
private fun AppEmptyStatePreview() {
    TrainingPlannerTheme2 {
        AppEmptyState(
            title = "No exercises found",
            subtitle = "Try adjusting your search terms or filter criteria to discover more movements.",
            actionLabel = "Reset Filters",
            onActionClick = {}
        )
    }
}
