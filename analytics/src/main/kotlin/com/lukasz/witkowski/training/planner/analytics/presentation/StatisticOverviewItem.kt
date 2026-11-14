package com.lukasz.witkowski.training.planner.analytics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.lukasz.witkowski.training.planner.analytics.R
import com.lukasz.witkowski.training.planner.analytics.domain.models.TrainingStatisticsOverview
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import java.util.Date


@Composable
internal fun StatisticOverviewItem(
    overview: TrainingStatisticsOverview,
    onItemClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        OverviewHeader(
            date = overview.date,
            completenessRate = overview.completenessRate
        )
        Spacer(Modifier.height(Dimens.spaceSm))
        AppCard(

        ) {
            Column(
                modifier = Modifier.padding(Dimens.margin)
            ) {
                OverviewCardHeader(overview)
            }
        }


    }
}

@Composable
private fun OverviewHeader(
    date: Date,
    completenessRate: Double
) {
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
private fun FormattedSessionDate(
    date: Date,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val label = remember(date) { date.toFormattedDateLabel(context) }
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) {
                append(label.primary)
            }
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                append(" · ")
                append(label.secondary)
            }
        },
    )
}

@Composable
private fun OverviewCardHeader(
    overview: TrainingStatisticsOverview,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
            modifier = Modifier.padding(end = Dimens.spaceSm)
        ) {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = overview.totalTime.toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val volumeText = overview.totalVolumeKg.takeIf { it > 0 }?.formatUpToTwoDecimals()?.plus(stringResource(R.string.kg))
            ?: stringResource(R.string.bodyweight)
        Text(
            text = stringResource(R.string.volume_summary, volumeText),
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
         )
        overview.rpe?.let {
            EffortPill(it, modifier = Modifier.padding(start = Dimens.spaceSm))
        }
    }
}




@AppPreview
@Composable
private fun StatisticOverviewItemsPreview() {
    TrainingPlannerTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)) {
            PREVIEW_TRAINING_STATISTICS_OVERVIEWS.forEach {
                StatisticOverviewItem(
                    overview = it,
                    onItemClicked = {}
                )
            }
        }
    }
}