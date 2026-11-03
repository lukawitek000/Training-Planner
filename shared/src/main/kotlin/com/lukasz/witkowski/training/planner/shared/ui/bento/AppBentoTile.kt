package com.lukasz.witkowski.training.planner.shared.ui.bento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard

/**
 * Bento grid metric tile displaying summary performance numbers.
 */
@Composable
fun AppBentoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    AppCard(
        modifier = modifier.defaultMinSize(minHeight = Dimens2.bentoTileMinHeight),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens2.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@AppPreview
@Composable
private fun AppBentoTilePreview() {
    TrainingPlannerTheme2 {
        AppBentoTile(
            label = "Total Volume",
            value = "12,450 kg",
            subtitle = "+5% vs previous session",
            modifier = Modifier.padding(Dimens2.spaceMd)
        )
    }
}
