package com.lukasz.witkowski.training.planner.shared.ui.appIcon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2

/**
 * Feature icon box composable supporting normal and premium badge modes according to design specification.
 *
 * Spec Highlights:
 * - Container: 56dp x 56dp (w-14 h-14), rounded-2xl / large shape (16dp), surfaceContainerHigh background, 1dp outlineVariant (30% alpha) border, shadow-lg.
 * - Main Icon: 30dp size, primaryContainer color.
 * - Premium Badge: 20dp x 20dp (w-5 h-5), rounded-full shape, primaryContainer background, onPrimary icon color, bottom-right offset (4dp), shadow-sm.
 */
@Composable
fun AppIconBox(
    modifier: Modifier = Modifier,
    mainIcon: ImageVector = Icons.Default.FitnessCenter,
    mainIconContentDescription: String? = null,
    isPremium: Boolean = false,
    badgeIcon: ImageVector = Icons.Default.Bolt,
    badgeIconContentDescription: String? = null,
    mainIconTint: Color = MaterialTheme.colorScheme.primaryContainer,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    badgeContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    badgeIconTint: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd,
    ) {
        Surface(
            modifier = Modifier.size(Dimens2.iconContainerSize),
            shape = MaterialTheme.shapes.large,
            color = containerColor,
            border = BorderStroke(
                width = Dimens2.thinBorder,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            ),
            shadowElevation = Dimens2.shadowLg,
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = mainIcon,
                    contentDescription = mainIconContentDescription,
                    tint = mainIconTint,
                    modifier = Modifier.size(Dimens2.iconSize30),
                )
            }
        }

        if (isPremium) {
            Surface(
                modifier = Modifier
                    .size(Dimens2.inputIconSize)
                    .offset(x = Dimens2.spaceXs, y = Dimens2.spaceXs),
                shape = CircleShape,
                color = badgeContainerColor,
                shadowElevation = Dimens2.shadowSm,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = badgeIconContentDescription,
                        tint = badgeIconTint,
                        modifier = Modifier.size(Dimens2.iconSize13),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A120A)
@Composable
private fun AppIconBoxPreview() {
    TrainingPlannerTheme2 {
        Row(
            modifier = Modifier.padding(Dimens2.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceLg),
        ) {
            // Normal Icon Box
            AppIconBox(
                mainIcon = Icons.Default.FitnessCenter,
                isPremium = false,
            )

            // Premium Icon Box (with bolt badge)
            AppIconBox(
                mainIcon = Icons.Default.FitnessCenter,
                isPremium = true,
                badgeIcon = Icons.Default.Bolt,
            )
        }
    }
}
