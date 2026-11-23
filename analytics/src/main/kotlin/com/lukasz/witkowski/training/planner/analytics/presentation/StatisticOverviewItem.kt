package com.lukasz.witkowski.training.planner.analytics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.times
import com.lukasz.witkowski.training.planner.analytics.R
import com.lukasz.witkowski.training.planner.analytics.domain.models.ExerciseStatisticsOverview
import com.lukasz.witkowski.training.planner.analytics.domain.models.TrainingStatisticsOverview
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.time.LocalTimeProvider
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import com.lukasz.witkowski.training.planner.shared.time.formatDuration
import com.lukasz.witkowski.training.planner.shared.time.toRelativeDateLabel
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.utils.formatUpToTwoDecimals
import com.lukasz.witkowski.training.planner.shared.utils.formatVolumeText
import kotlin.time.Instant

@Composable
internal fun StatisticOverviewItem(
    overview: TrainingStatisticsOverview,
    isFirst: Boolean,
    isLast: Boolean,
    onItemClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dotCenterY by remember { mutableFloatStateOf(0f) }

    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val dotColor = MaterialTheme.colorScheme.primaryContainer
    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val gutterCenterX = (Dimens.gutterWidth - 2 * Dimens.spaceSm).toPx()
                val dotRadius = Dimens.dotRadius.toPx()
                val strokeWidth = Dimens.timelineStrokeWidth.toPx()

                val lineStartY = if (isFirst) dotCenterY else 0f
                val lineEndY = if (isLast) dotCenterY else size.height

                if (lineEndY > lineStartY) {
                    drawLine(
                        color = lineColor,
                        start = Offset(x = gutterCenterX, y = lineStartY),
                        end = Offset(x = gutterCenterX, y = lineEndY),
                        strokeWidth = strokeWidth
                    )
                }

                val center = Offset(x = gutterCenterX, y = dotCenterY)
                val colors = listOf(
                    dotColor,
                    dotColor.copy(alpha = 0.9f),
                    dotColor.copy(alpha = 0.6f),
                    dotColor.copy(alpha = 0.2f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = colors,
                        center = center,
                        radius = dotRadius
                    ),
                    radius = dotRadius,
                    center = center
                )
            }
            .padding(
                start = Dimens.gutterWidth,
                bottom = if (isLast) Dimens.zeroDp else Dimens.spaceMd
            )
    ) {
        Box(
            modifier = Modifier.onPlaced { coordinates ->
                dotCenterY = coordinates.boundsInParent().center.y
            }
        ) {
            OverviewHeader(
                date = overview.date,
                completenessRate = overview.completenessRate
            )
        }
        Spacer(Modifier.height(Dimens.spaceSm))
        AppCard {
            Column(
                modifier = Modifier.padding(Dimens.spaceSm),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
            ) {
                OverviewCardHeader(overview)
                TrainingSessionSummary(overview)
                overview.note?.takeIf { it.isNotBlank() }?.let {
                    SessionNotes(
                        notes = it
                    )
                }
                ExercisesOverviews(
                    overview.exerciseOverviews
                )
                DetailsButton(
                    onClick = onItemClicked
                )
            }
        }
    }
}

@Composable
private fun OverviewHeader(date: Instant, completenessRate: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FormattedSessionDate(date)
        Spacer(Modifier.weight(1f))
        CompletenessPill(completenessRate)
    }
}

@Composable
private fun FormattedSessionDate(date: Instant, modifier: Modifier = Modifier) {
    val label = date.toRelativeDateLabel()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        Text(
            text = label.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label.secondary,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OverviewCardHeader(
    overview: TrainingStatisticsOverview,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSize24)
            )
            Text(
                text = stringResource(R.string.session_stats),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        overview.rpe?.let {
            EffortPill(it, modifier = Modifier.padding(start = Dimens.spaceSm))
        }
    }
}

@Composable
private fun TrainingSessionSummary(
    overview: TrainingStatisticsOverview,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().background(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.medium
        )
            .padding(Dimens.spaceSm),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatisticWithLabelAndIcon(
            label = stringResource(R.string.duration_label),
            icon = Icons.Default.AccessTime,
            statistic = overview.totalTime.formatDuration(),
            modifier = Modifier.weight(1f)
        )

        val volumeText = overview.totalVolume.formatVolumeText()
        StatisticWithLabelAndIcon(
            label = stringResource(R.string.volume_label),
            icon = Icons.Default.BarChart,
            statistic = volumeText,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatisticWithLabelAndIcon(
    label: String,
    icon: ImageVector,
    statistic: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(Dimens.spaceSm)
            )
        }
        Spacer(Modifier.width(Dimens.spaceSm))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Dimens.spaceXs))
            Text(
                text = statistic,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ExercisesOverviews(
    overviews: List<ExerciseStatisticsOverview>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
    ) {
        overviews.forEach { overview ->
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = Dimens.spaceSm, vertical = Dimens.spaceXs)
            ) {
                Text(overview.exerciseName)
                Spacer(Modifier.width(Dimens.spaceSm))
                Text(
                    stringResource(
                        R.string.exercise_overview_summary,
                        overview.completenessRate.formatUpToTwoDecimals(),
                        overview.totalTime.formatDuration()
                    )
                )
            }
        }
    }
}

@Composable
private fun DetailsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Insights,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.inspect_details),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = Dimens.spaceSm)
        )
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@AppPreview
@Composable
private fun StatisticOverviewItemsPreview() {
    TrainingPlannerTheme {
        CompositionLocalProvider(
            LocalTimeProvider provides TestTimeProvider(Instant.parse("2026-09-10T12:00:00Z"))
        ) {
            Column {
                PREVIEW_TRAINING_STATISTICS_OVERVIEWS.forEachIndexed { index, overview ->
                    StatisticOverviewItem(
                        overview = overview,
                        isFirst = index == 0,
                        isLast = index == PREVIEW_TRAINING_STATISTICS_OVERVIEWS.lastIndex,
                        onItemClicked = {}
                    )
                }
            }
        }
    }
}
