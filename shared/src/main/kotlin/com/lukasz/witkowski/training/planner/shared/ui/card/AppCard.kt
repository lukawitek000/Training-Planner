package com.lukasz.witkowski.training.planner.shared.ui.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Container card composable styled according to design specification.
 *
 * Spec Highlights:
 * - Background color: surfaceContainer
 * - Shape: rounded-xl / medium shape
 * - Elevation: shadow-sm / 2dp [Dimens2.shadowSm]
 * - Content: Flex Column scope
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    border: BorderStroke? = null,
    elevation: Dp = Dimens2.shadowSm,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        border = border,
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
        ),
        content = content,
    )
}

@AppPreview
@Composable
private fun AppCardPreview() {
    TrainingPlannerTheme2 {
        AppCard(modifier = Modifier.padding(Dimens2.spaceMd)) {
            Text(
                text = "Card Section 1",
                modifier = Modifier.padding(Dimens2.spaceMd),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppCardDivider()
            Text(
                text = "Card Section 2",
                modifier = Modifier.padding(Dimens2.spaceMd),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
