package com.lukasz.witkowski.training.planner.shared.ui.badges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Compact metadata tag composable styled according to Stitch design specification.
 *
 * Spec Highlights:
 * - Container: [MaterialTheme.colorScheme.surfaceContainerHigh] (or custom [containerColor])
 * - Shape: [CircleShape]
 * - Icon: optional leading [Dimens2.iconSize12] icon
 * - Text: [MaterialTheme.typography.labelSmall]
 */
@Composable
fun AppMetadataPill(
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconTint: Color = contentColor
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier =
            Modifier.padding(
                horizontal = Dimens2.spaceSm,
                vertical = Dimens2.badgeVerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = leadingIconContentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(Dimens2.iconSize12)
                )
                Spacer(modifier = Modifier.width(Dimens2.spaceXs))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

@AppPreview
@Composable
private fun AppMetadataPillPreview() {
    TrainingPlannerTheme2 {
        Row(
            modifier = Modifier.padding(Dimens2.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            AppMetadataPill(
                text = "Local",
                leadingIcon = Icons.Default.Storage,
                contentColor = MaterialTheme.colorScheme.secondary
            )
            AppMetadataPill(
                text = "Cloud",
                leadingIcon = Icons.Default.Cloud,
                contentColor = MaterialTheme.colorScheme.tertiaryFixedDim
            )
            AppMetadataPill(
                text = "Owned by me",
                leadingIcon = Icons.Default.Person,
                contentColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}
