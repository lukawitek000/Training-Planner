package com.lukasz.witkowski.training.planner.exercise.presentation.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Recommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendationLevel
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.Success
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.theme.Warning
import com.lukasz.witkowski.training.planner.shared.ui.timepicker.AppTimePickerBottomSheet
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun RecommendedParametersOverview(
    recommendations: List<Recommendation>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        Text(
            text = stringResource(R.string.recommended_parameters),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        recommendations.forEach {
            RecommendedParametersCard(
                recommendation = it,
                extendedCardContent = {
                    RecommendedParametersExtendedSummary(it.parameters)
                }
            )
        }
    }
}

@Composable
fun RecommendedParametersForm(
    recommendations: List<Recommendation>,
    onRecommendationChange: (ExerciseEditingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        Text(
            text = stringResource(R.string.recommended_parameters),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.recommended_parameters_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        recommendations.forEach {
            RecommendedParametersCard(
                recommendation = it,
                extendedCardContent = {
                    RecommendedParametersInputFields(
                        it,
                        onRecommendationChange
                    )
                }
            )
        }
    }
}

@Composable
private fun RecommendedParametersCard(
    recommendation: Recommendation,
    extendedCardContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = Dimens2.thinBorder,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens2.spaceSm),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            RecommendedParametersCardHeader(
                isExpanded = isExpanded,
                toggleExpansion = { isExpanded = !isExpanded },
                recommendation = recommendation,
                modifier = Modifier.testTag("${recommendation.level.name}_CardHeader")
            )
            AnimatedVisibility(visible = isExpanded) {
                extendedCardContent()
            }
        }
    }
}

@Composable
private fun RecommendedParametersInputFields(
    recommendation: Recommendation,
    onRecommendationChange: (ExerciseEditingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    ExerciseParametersInputFields(
        parameters = recommendation.parameters,
        testTagPrefix = recommendation.level.name,
        onParametersIntent = {
            onRecommendationChange(it.toExerciseEditingEvent(recommendation.level))
        },
        modifier = modifier
    )
}

@Composable
fun ExerciseParametersInputFields(
    parameters: RecommendedParameters,
    testTagPrefix: String,
    onParametersIntent: (ExerciseParametersIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRestTimePicker by rememberSaveable { mutableStateOf(false) }
    var showHoldTimePicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        // Row 1: Sets, Reps, Weight
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            ParameterInputField(
                value = parameters.sets,
                onValueChange = { onParametersIntent(ExerciseParametersIntent.SetsChanged(it)) },
                label = buildRequiredString(stringResource(R.string.sets)),
                modifier = Modifier.weight(1f),
                testTag = "${testTagPrefix}_SetsField"
            )
            ParameterInputField(
                value = parameters.reps,
                onValueChange = { onParametersIntent(ExerciseParametersIntent.RepsChanged(it)) },
                label = buildRequiredString(stringResource(R.string.reps)),
                modifier = Modifier.weight(1f),
                testTag = "${testTagPrefix}_RepsField"
            )
            ParameterInputField(
                value = parameters.weightInKg,
                onValueChange = { onParametersIntent(ExerciseParametersIntent.WeightChanged(it)) },
                label = stringResource(R.string.weight),
                unit = stringResource(R.string.kg),
                modifier = Modifier.weight(1f),
                testTag = "${testTagPrefix}_WeightField"
            )
        }

        // Row 2: Hold / Time, Rest time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            ParameterInputField(
                value = parameters.time?.inWholeSeconds?.toInt(),
                onValueChange = {
                    onParametersIntent(ExerciseParametersIntent.TimeChanged(it?.seconds))
                },
                label = stringResource(R.string.hold_time),
                unit = stringResource(R.string.second_abbrv),
                modifier = Modifier.weight(1f),
                testTag = "${testTagPrefix}_HoldTimeField",
                trailingIcon = {
                    IconButton(
                        onClick = { showHoldTimePicker = true },
                        modifier = Modifier
                            .size(Dimens2.inputIconSize)
                            .testTag("${testTagPrefix}_HoldTimePickerButton")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = stringResource(R.string.hold_time),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens2.iconSize16)
                        )
                    }
                }
            )
            ParameterInputField(
                value = parameters.restTime?.inWholeSeconds?.toInt(),
                onValueChange = {
                    onParametersIntent(ExerciseParametersIntent.RestTimeChanged(it?.seconds))
                },
                label = buildRequiredString(stringResource(R.string.rest_time)),
                unit = stringResource(R.string.second_abbrv),
                modifier = Modifier.weight(1f),
                testTag = "${testTagPrefix}_RestTimeField",
                trailingIcon = {
                    IconButton(
                        onClick = { showRestTimePicker = true },
                        modifier = Modifier
                            .size(Dimens2.inputIconSize)
                            .testTag("${testTagPrefix}_RestTimePickerButton")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = stringResource(R.string.rest_time),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens2.iconSize16)
                        )
                    }
                }
            )
        }

        // Row 3: Helper note
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Dimens2.spaceXs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(Dimens2.iconSize16)
            )
            Text(
                text = stringResource(R.string.hold_time_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }

    if (showHoldTimePicker) {
        AppTimePickerBottomSheet(
            initialSeconds = parameters.time?.inWholeSeconds?.toInt(),
            contextBadgeText = stringResource(R.string.hold),
            allowNoTime = true,
            onDismissRequest = { showHoldTimePicker = false },
            onSecondsConfirmed = { selectedSeconds ->
                onParametersIntent(ExerciseParametersIntent.TimeChanged(selectedSeconds?.seconds))
                showHoldTimePicker = false
            }
        )
    }

    if (showRestTimePicker) {
        AppTimePickerBottomSheet(
            initialSeconds = parameters.restTime?.inWholeSeconds?.toInt() ?: 60,
            contextBadgeText = stringResource(R.string.rest_time),
            allowNoTime = false,
            onDismissRequest = { showRestTimePicker = false },
            onSecondsConfirmed = { selectedSeconds ->
                onParametersIntent(
                    ExerciseParametersIntent.RestTimeChanged(selectedSeconds?.seconds)
                )
                showRestTimePicker = false
            }
        )
    }
}

sealed interface ExerciseParametersIntent {
    fun toExerciseEditingEvent(level: RecommendationLevel): ExerciseEditingEvent

    data class SetsChanged(val sets: Int?) : ExerciseParametersIntent {
        override fun toExerciseEditingEvent(level: RecommendationLevel): ExerciseEditingEvent =
            ExerciseEditingEvent.RecommendedSetsChanged(sets, level)
    }

    data class RepsChanged(val reps: Int?) : ExerciseParametersIntent {
        override fun toExerciseEditingEvent(level: RecommendationLevel): ExerciseEditingEvent =
            ExerciseEditingEvent.RecommendedRepsChanged(reps, level)
    }

    data class RestTimeChanged(val restTime: Duration?) : ExerciseParametersIntent {
        override fun toExerciseEditingEvent(level: RecommendationLevel): ExerciseEditingEvent =
            ExerciseEditingEvent.RecommendedRestTimeChanged(restTime, level)
    }

    data class TimeChanged(val time: Duration?) : ExerciseParametersIntent {
        override fun toExerciseEditingEvent(level: RecommendationLevel): ExerciseEditingEvent =
            ExerciseEditingEvent.RecommendedTimeChanged(time, level)
    }

    data class WeightChanged(val weight: Int?) : ExerciseParametersIntent {
        override fun toExerciseEditingEvent(level: RecommendationLevel): ExerciseEditingEvent =
            ExerciseEditingEvent.RecommendedWeightChanged(weight, level)
    }
}

private fun buildRequiredString(str: String): String = buildString {
    append(str)
    append(" *")
}

@Composable
fun RecommendedParametersCardHeader(
    isExpanded: Boolean,
    toggleExpansion: () -> Unit,
    recommendation: Recommendation,
    modifier: Modifier = Modifier,
    isExpandable: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { toggleExpansion() }
            .padding(start = Dimens2.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        Box(Modifier.weight(0.1f)) {
            Box(
                modifier = Modifier
                    .size(Dimens2.spaceSm)
                    .background(recommendation.level.toColor(), shape = CircleShape)
            )
        }
        Text(
            text = stringResource(recommendation.level.nameRes),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.8f)
        )
        Text(
            text = if (isExpanded) "" else buildStringOverview(recommendation),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (isExpandable) {
            val icon = if (isExpanded) {
                Icons.Default.ExpandLess
            } else {
                Icons.Default.ExpandMore
            }
            IconButton(onClick = toggleExpansion) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecommendationLevel.toColor(): Color = when (this) {
    RecommendationLevel.BEGINNER -> Success
    RecommendationLevel.INTERMEDIATE -> Warning
    RecommendationLevel.ADVANCED -> MaterialTheme.colorScheme.error
}

@Composable
private fun buildStringOverview(recommendation: Recommendation): String =
    recommendation.parameters.let {
        if (!it.areValid()) {
            stringResource(R.string.not_set)
        } else {
            buildStringOverview(
                sets = it.sets!!,
                reps = it.reps!!,
                restTime = it.restTime!!,
                weightInKg = it.weightInKg,
                time = it.time
            )
        }
    }

@Composable
fun buildStringOverview(
    sets: Int,
    reps: Int,
    restTime: Duration,
    weightInKg: Int?,
    time: Duration? = null
): String = when {
    time != null && weightInKg != null -> {
        stringResource(
            R.string.recommendation_overview_with_hold,
            sets,
            reps,
            time.inWholeSeconds,
            restTime.inWholeSeconds,
            weightInKg
        )
    }
    time != null -> {
        stringResource(
            R.string.recommendation_with_hold_no_weight,
            sets,
            reps,
            time.inWholeSeconds,
            restTime.inWholeSeconds
        )
    }
    weightInKg != null -> {
        stringResource(
            R.string.recommendation_overview,
            sets,
            reps,
            restTime.inWholeSeconds,
            weightInKg
        )
    }
    else -> {
        stringResource(
            R.string.recommendation_no_weight,
            sets,
            reps,
            restTime.inWholeSeconds
        )
    }
}

@Composable
private fun ParameterInputField(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    unit: String = "",
    testTag: String = "",
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            modifier = Modifier.padding(bottom = Dimens2.spaceXs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        OutlinedTextField(
            value = value?.toString() ?: "",
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() }
                val sanitized = if (filtered.startsWith('0')) {
                    filtered.trimStart('0')
                } else {
                    filtered
                }
                onValueChange(sanitized.toIntOrNull())
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .defaultMinSize(minWidth = 40.dp)
                .testTag(testTag),
            suffix = if (unit.isNotEmpty()) {
                { Text(unit, style = MaterialTheme.typography.labelSmall) }
            } else {
                null
            },
            trailingIcon = trailingIcon
        )
    }
}

@Composable
private fun RecommendedParametersExtendedSummary(
    params: RecommendedParameters,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimens2.spaceSm),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ParameterOverview(
            value = params.sets,
            label = "${stringResource(R.string.sets)}:"
        )
        ParameterOverview(
            value = params.reps,
            label = "${stringResource(R.string.reps)}:"
        )
        if (params.time != null) {
            ParameterOverview(
                value = params.time.inWholeSeconds.toInt(),
                label = "${stringResource(R.string.hold)}:",
                unit = stringResource(R.string.second_abbrv)
            )
        }
        ParameterOverview(
            value = params.restTime?.inWholeSeconds?.toInt(),
            label = "${stringResource(R.string.rest_time)}:",
            unit = stringResource(R.string.second_abbrv)
        )
        ParameterOverview(
            value = params.weightInKg,
            label = "${stringResource(R.string.weight)}:",
            unit = stringResource(R.string.kg)
        )
    }
}

@Composable
private fun ParameterOverview(
    value: Int?,
    label: String,
    modifier: Modifier = Modifier,
    unit: String = ""
) {
    if (value == null) return
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(Dimens2.spaceXs))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (unit.isNotEmpty()) {
            Spacer(Modifier.width(Dimens2.spaceXs / 2))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun RecommendedParametersFormPreview() {
    TrainingPlannerTheme2 {
        RecommendedParametersForm(
            recommendations = PREVIEW_RECOMMENDED_PARAMETERS,
            onRecommendationChange = {}
        )
    }
}

@Preview
@Composable
private fun RecommendedParametersOverviewPreview() {
    TrainingPlannerTheme2 {
        RecommendedParametersOverview(
            recommendations = PREVIEW_RECOMMENDED_PARAMETERS,
            modifier = Modifier
        )
    }
}

val PREVIEW_RECOMMENDED_PARAMETERS = listOf(
    Recommendation(
        RecommendationLevel.BEGINNER,
        parameters = RecommendedParameters(
            sets = 3,
            reps = 10,
            restTime = 3.minutes
        )
    ),
    Recommendation(
        RecommendationLevel.INTERMEDIATE,
        parameters = RecommendedParameters(
            sets = 3,
            reps = 10,
            restTime = 1.minutes,
            weightInKg = 30
        )
    ),
    Recommendation(RecommendationLevel.ADVANCED)
)
