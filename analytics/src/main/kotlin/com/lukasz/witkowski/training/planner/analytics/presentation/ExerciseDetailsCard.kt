package com.lukasz.witkowski.training.planner.analytics.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import com.lukasz.witkowski.training.planner.analytics.R
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.ErrorContainer
import com.lukasz.witkowski.training.planner.shared.theme.Success
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.theme.Warning
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.utils.Weight
import com.lukasz.witkowski.training.planner.shared.utils.formatVolumeText
import kotlin.time.Duration

private const val PARTIAL_COMPLETION_RATIO = 0.6
private const val REST_PARTIAL_TOLERANCE_RATIO = 1.25

@Composable
internal fun ExerciseDetailsCard(
    statistics: ExerciseStatistics,
    index: Int,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable(index) { mutableStateOf(true) }
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceSm)
        ) {
            ExerciseCardHeader(
                exerciseName = statistics.exerciseName,
                completenessRate = statistics.completenessRate,
                totalTime = statistics.totalTime,
                index = index,
                isExpanded = isExpanded,
                onToggleExpansion = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(
                visible = isExpanded
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                    modifier = Modifier.padding(top = Dimens.spaceMd)
                ) {
                    PlannedConfiguration(
                        plannedSets = statistics.plannedSets,
                        plannedConfig = statistics.plannedConfig,
                        plannedRestTime = statistics.plannedRestTime
                    )
                    statistics.attemptsStatistics.forEachIndexed { setIndex, setStatistics ->
                        ExerciseSetStatistics(
                            index = setIndex,
                            statistics = setStatistics,
                            plannedConfig = statistics.plannedConfig,
                            fallbackPlannedRestTime = statistics.plannedRestTime
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCardHeader(
    exerciseName: String,
    completenessRate: Double,
    totalTime: Duration,
    index: Int,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = if (isExpanded) {
        Icons.Default.ExpandLess
    } else {
        Icons.Default.ExpandMore
    }
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onToggleExpansion)
                .padding(end = Dimens.spaceSm, top = Dimens.spaceXs, bottom = Dimens.spaceXs)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            ExerciseNumber(
                number = index + 1
            )
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier,
                tint = MaterialTheme.colorScheme.outline
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            CompletenessPill(
                completenessRate = completenessRate,
                includeLabel = false
            )
            Text(
                text = totalTime.toString()
            )
        }
    }
}

@Composable
private fun ExerciseNumber(number: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .padding(Dimens.spaceSm)
            .size(Dimens.spaceXl),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun ExerciseSetStatistics(
    index: Int,
    statistics: ExerciseAttemptStatistics,
    plannedConfig: SessionExerciseConfiguration,
    fallbackPlannedRestTime: Duration,
    modifier: Modifier = Modifier
) {
    val plannedRest = statistics.plannedRestTime
        ?: fallbackPlannedRestTime.takeIf { it > Duration.ZERO }
    val showLoad = statistics.actualWeight != null || plannedConfig.weight != null
    val showHold = statistics.actualTime != null || plannedConfig.holdTime != null
    val showRest = statistics.restTime != null || plannedRest != null

    AppCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spaceSm),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            SetHeader(
                statistics = statistics,
                index = index
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
                ) {
                    if (showLoad) {
                        val actualLoad = statistics.actualWeight ?: Weight.ZERO
                        val targetLoad = plannedConfig.weight
                        ParameterLayout(
                            label = stringResource(R.string.parameter_load_label),
                            value = actualLoad.formatVolumeText(),
                            targetValue = targetLoad?.formatVolumeText(),
                            parameterColors = ParameterColors.forTargetMinimum(
                                completed = statistics.completed,
                                actual = actualLoad.inKilograms,
                                target = targetLoad?.inKilograms
                            )
                        )
                    }
                    if (!plannedConfig.isIsometric || statistics.actualReps > 0) {
                        val targetReps = plannedConfig.reps.takeIf { it > 0 }
                        ParameterLayout(
                            label = stringResource(R.string.parameter_reps_label),
                            value = statistics.actualReps.toString(),
                            targetValue = targetReps?.toString(),
                            parameterColors = ParameterColors.forTargetMinimum(
                                completed = statistics.completed,
                                actual = statistics.actualReps.toDouble(),
                                target = targetReps?.toDouble()
                            )
                        )
                    }
                }
                if (showHold || showRest) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
                    ) {
                        if (showHold) {
                            val actualHold = statistics.actualTime ?: Duration.ZERO
                            val targetHold = plannedConfig.holdTime
                            ParameterLayout(
                                label = stringResource(R.string.parameter_hold_time_label),
                                value = actualHold.toString(),
                                targetValue = targetHold?.toString(),
                                parameterColors = ParameterColors.forTargetMinimum(
                                    completed = statistics.completed,
                                    actual = actualHold.inWholeMilliseconds.toDouble(),
                                    target = targetHold?.inWholeMilliseconds?.toDouble()
                                )
                            )
                        }
                        if (showRest) {
                            val actualRest = statistics.restTime ?: Duration.ZERO
                            ParameterLayout(
                                label = stringResource(R.string.parameter_rest_taken_label),
                                value = actualRest.toString(),
                                targetValue = plannedRest?.toString(),
                                parameterColors = ParameterColors.forRestDuration(
                                    completed = statistics.completed,
                                    actual = actualRest,
                                    target = plannedRest
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetHeader(statistics: ExerciseAttemptStatistics, index: Int) {
    val icon = if (statistics.completed) {
        Icons.Default.Done
    } else {
        Icons.Default.Block
    }
    val iconColor = if (statistics.completed) {
        Success
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val titleSuffix = if (statistics.completed) {
        stringResource(R.string.completed)
    } else {
        stringResource(R.string.skipped)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(Dimens.iconSize24)
                .background(
                    color = iconColor.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .padding(Dimens.spaceXs),
            tint = iconColor
        )
        Text(
            text = stringResource(R.string.set_label, (index + 1), titleSuffix),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.weight(1f))
        statistics.effort?.let { effort ->
            EffortPill(
                rpe = effort.sessionRpe,
                textFactory = { label, score ->
                    stringResource(
                        R.string.effort_short_summary,
                        label,
                        score
                    )
                }
            )
        }
    }
}

@Composable
private fun PlannedConfiguration(
    plannedSets: Int,
    plannedConfig: SessionExerciseConfiguration,
    plannedRestTime: Duration,
    modifier: Modifier = Modifier
) {
    val plannedHold = plannedConfig.holdTime
    val chips = buildList {
        add(stringResource(R.string.planned_sets_format, plannedSets))
        if (plannedHold != null && plannedHold > Duration.ZERO) {
            add(stringResource(R.string.planned_hold_format, plannedHold.toString()))
        } else {
            add(stringResource(R.string.planned_reps_format, plannedConfig.reps))
        }
        add(plannedConfig.weight.formatVolumeText())
        if (plannedRestTime > Duration.ZERO) {
            add(stringResource(R.string.planned_rest_format, plannedRestTime.toString()))
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = Dimens.spaceSm, vertical = Dimens.spaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.iconSize16)
        )
        Text(
            text = stringResource(R.string.planned_config_label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceXxs)
        ) {
            chips.forEachIndexed { idx, chip ->
                val text = if (idx < chips.lastIndex) "$chip ·" else chip
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RowScope.ParameterLayout(
    label: String,
    value: String,
    parameterColors: ParameterColors,
    targetValue: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = Dimens.spaceSm, vertical = Dimens.spaceXs),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        ValueWithTargetWrapRow {
            Text(
                text = value,
                color = parameterColors.color,
                style = MaterialTheme.typography.titleLarge
            )
            targetValue?.let { target ->
                TargetValue(
                    target = target,
                    color = parameterColors.color
                )
            }
        }
    }
}

@Composable
private fun TargetValue(target: String, color: Color, modifier: Modifier = Modifier) {
    val backgroundColor = color.copy(alpha = 0.2f)
    Text(
        text = stringResource(R.string.target_prefix, target),
        color = color,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small
            )
            .padding(Dimens.spaceXxs)
    )
}

@Composable
private fun ValueWithTargetWrapRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val horizontalGap = Dimens.spaceXs
    val verticalGap = Dimens.spaceXs
    val measurePolicy = remember(horizontalGap, verticalGap) {
        object : MeasurePolicy {
            override fun MeasureScope.measure(
                measurables: List<Measurable>,
                constraints: Constraints
            ): MeasureResult {
                val gapPx = horizontalGap.roundToPx()
                val vGapPx = verticalGap.roundToPx()
                val maxWidth = constraints.maxWidth
                val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

                if (measurables.isEmpty()) return layout(0, 0) {}

                if (measurables.size == 1) {
                    val single = measurables[0].measure(looseConstraints)
                    return layout(maxWidth, single.height) {
                        single.placeRelative(0, 0)
                    }
                }

                val first = measurables[0].measure(looseConstraints)
                val second = measurables[1].measure(looseConstraints)
                val fitsOnOneLine = first.width + gapPx + second.width <= maxWidth

                return if (fitsOnOneLine) {
                    val rowHeight = maxOf(first.height, second.height)
                    layout(maxWidth, rowHeight) {
                        first.placeRelative(0, (rowHeight - first.height) / 2)
                        second.placeRelative(
                            x = maxWidth - second.width,
                            y = (rowHeight - second.height) / 2
                        )
                    }
                } else {
                    val totalHeight = first.height + vGapPx + second.height
                    layout(maxWidth, totalHeight) {
                        first.placeRelative(0, 0)
                        second.placeRelative(
                            x = maxWidth - second.width,
                            y = first.height + vGapPx
                        )
                    }
                }
            }

            override fun IntrinsicMeasureScope.maxIntrinsicHeight(
                measurables: List<IntrinsicMeasurable>,
                width: Int
            ): Int {
                if (measurables.isEmpty()) return 0
                val gapPx = horizontalGap.roundToPx()
                val vGapPx = verticalGap.roundToPx()

                if (measurables.size == 1) {
                    return measurables[0].maxIntrinsicHeight(width)
                }

                val firstWidth = measurables[0].maxIntrinsicWidth(Constraints.Infinity)
                val secondWidth = measurables[1].maxIntrinsicWidth(Constraints.Infinity)
                val firstHeight = measurables[0].maxIntrinsicHeight(width)
                val secondHeight = measurables[1].maxIntrinsicHeight(width)

                return if (firstWidth + gapPx + secondWidth <= width) {
                    maxOf(firstHeight, secondHeight)
                } else {
                    firstHeight + vGapPx + secondHeight
                }
            }
        }
    }

    Layout(
        content = content,
        modifier = modifier,
        measurePolicy = measurePolicy
    )
}

internal sealed interface ParameterColors {
    val color: Color

    data object Completed : ParameterColors {
        override val color: Color = Success
    }

    data object PartiallyCompleted : ParameterColors {
        override val color: Color = Warning
    }

    data object CriticallyIncompleted : ParameterColors {
        override val color: Color = ErrorContainer
    }

    companion object {
        fun forTargetMinimum(completed: Boolean, actual: Double, target: Double?): ParameterColors {
            if (!completed) return CriticallyIncompleted
            if (target == null || target <= 0.0) return Completed
            return when {
                actual >= target -> Completed
                actual >= target * PARTIAL_COMPLETION_RATIO -> PartiallyCompleted
                else -> CriticallyIncompleted
            }
        }

        fun forRestDuration(
            completed: Boolean,
            actual: Duration,
            target: Duration?
        ): ParameterColors {
            if (!completed && actual == Duration.ZERO) return CriticallyIncompleted
            if (target == null || target <= Duration.ZERO) return Completed
            val actualMs = actual.inWholeMilliseconds.toDouble()
            val targetMs = target.inWholeMilliseconds.toDouble()
            return when {
                actualMs <= targetMs -> Completed
                actualMs <= targetMs * REST_PARTIAL_TOLERANCE_RATIO -> PartiallyCompleted
                else -> CriticallyIncompleted
            }
        }
    }
}

@AppPreview
@Composable
private fun ExerciseDetailsCardPreview() {
    TrainingPlannerTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)) {
            PREVIEW_TRAINING_STATISTICS.exercisesStatistics.forEachIndexed { index, statistics ->
                ExerciseDetailsCard(
                    statistics = statistics,
                    index = index
                )
            }
        }
    }
}
