package com.lukasz.witkowski.training.planner.analytics.presentation

import android.text.Annotation
import android.text.Spanned
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.analytics.R
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.ErrorContainer
import com.lukasz.witkowski.training.planner.shared.theme.Success
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.theme.Warning
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import java.util.Date
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrainingStatisticsDetailsSheet(
    statistics: TrainingStatistics,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = BottomSheetDefaults.ExpandedShape,
        modifier = modifier
    ) {
        TrainingStatisticsDetailsContent(
            statistics = statistics,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun TrainingStatisticsDetailsContent(
    statistics: TrainingStatistics,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = Dimens.spaceSm)) {
        SheetHeader(
            date = statistics.date,
            totalTime = statistics.totalTime,
            onDismissRequest = onDismissRequest,
            modifier = Modifier.padding(bottom = Dimens.spaceMd)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            item(key = "session_summary") {
                SessionSummary(statistics)
            }
            statistics.note?.let { note ->
                item(key = "session_notes") {
                    SessionNotes(note)
                }
            }
            item(key = "exercise_breakdown_header") {
                Text(
                    text = stringResource(R.string.exercise_breakdown_header),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = Dimens.spaceSm)
                )
            }
            item {
                TargetComparisonLegend(
                    greenColor = ParameterColors.Completed.color,
                    amberColor = ParameterColors.PartiallyCompleted.color,
                    redColor = ParameterColors.CriticallyIncompleted.color
                )
            }
            itemsIndexed(statistics.exercisesStatistics, key = { _, stats -> stats.id.value.toString() }) { idx, stats ->
                ExerciseDetailsCard(
                    statistics = stats,
                    index = idx,
                    modifier = Modifier.padding(bottom = Dimens.spaceSm)
                )
            }
        }
    }
}

@Composable
private fun SheetHeader(
    date: Date,
    totalTime: Duration,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dateLabel = remember(date) { date.toFormattedDateLabel(context) }
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
        ) {
            Text(
                text = "Session Breakdown",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismissRequest) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close statistics details"
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
            Text(
                text = dateLabel.primary
            )
            Text(
                text = "($totalTime total)"
            )
        }

    }
}

@Composable
private fun SessionSummary(
    statistics: TrainingStatistics,
    modifier: Modifier = Modifier
) {
    val completenessRate = statistics.completenessRate
    val completenessStyle = remember(completenessRate) { CompletenessStyle.create(completenessRate) }
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            statistics.rpe?.let { rpe ->
                SessionSummaryCard(
                    label = stringResource(R.string.session_effort_label),
                    content = {
                        EffortPill(
                            rpe = rpe,
                            textFactory = { label, score ->
                                stringResource(R.string.effort_short_summary, label, score)
                            }
                        )
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            SessionSummaryCard(
                label = stringResource(R.string.completed),
                content = {
                    CompletenessPercentage(
                        completenessRate = completenessRate,
                        completenessStyle = completenessStyle,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            SessionSummaryCard(
                label = stringResource(R.string.session_total_vol_label),
                content = {
                    Text(
                        text = statistics.totalVolumeKg.formatVolumeText(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun SessionSummaryCard(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    subContent: @Composable () -> Unit = {}
) {
    AppCard(modifier) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(Dimens.spaceSm),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = Dimens.spaceSm)
            )
            content()
            subContent()
        }
    }
}

@Composable
private fun annotatedStringResource(
    @StringRes id: Int,
    styles: Map<String, SpanStyle>
): AnnotatedString {
    val resources = LocalResources.current
    val text = remember(id, resources) { resources.getText(id) }

    return remember(text, styles) {
        buildAnnotatedString {
            append(text.toString())

            val spanned = text as? Spanned ?: return@buildAnnotatedString
            val annotations = spanned.getSpans(0, spanned.length, Annotation::class.java)

            for (annotation in annotations) {
                if (annotation.key == "style") {
                    val spanStyle = styles[annotation.value] ?: continue
                    addStyle(
                        style = spanStyle,
                        start = spanned.getSpanStart(annotation),
                        end = spanned.getSpanEnd(annotation)
                    )
                }
            }
        }
    }
}

@Composable
fun TargetComparisonLegend(
    greenColor: Color,
    amberColor: Color,
    redColor: Color,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val styles = remember(onSurface, greenColor, amberColor, redColor) {
        mapOf(
            "bold" to SpanStyle(fontWeight = FontWeight.Bold, color = onSurface),
            "green" to SpanStyle(fontWeight = FontWeight.SemiBold, color = greenColor),
            "amber" to SpanStyle(fontWeight = FontWeight.SemiBold, color = amberColor),
            "red" to SpanStyle(fontWeight = FontWeight.SemiBold, color = redColor)
        )
    }

    val annotatedText = annotatedStringResource(
        id = R.string.target_comparison_legend,
        styles = styles
    )

    AppCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.CompareArrows,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@AppPreview
@Composable
private fun TrainingStatisticsDetailsContentPreview() {
    TrainingPlannerTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            TrainingStatisticsDetailsContent(
                statistics = PREVIEW_TRAINING_STATISTICS,
                onDismissRequest = {}
            )
        }
    }
}
