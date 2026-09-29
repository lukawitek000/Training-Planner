package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Recommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendationLevel
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import com.lukasz.witkowski.training.planner.ui.components.FormFieldLabel
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun RecommendedParametersOverview(
    recommendations: List<Recommendation>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        FormFieldLabel(text = stringResource(R.string.recommended_parameters))
        Text(
            text = stringResource(R.string.recommended_parameters_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
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
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(Dimens.normal)
        ) {
            RecommendedParametersCardHeader(
                isExpanded = isExpanded,
                toggleExpansion = { isExpanded = !isExpanded },
                recommendation = recommendation,
            )
            AnimatedVisibility(
                visible = isExpanded
            ) {
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
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.normal),
    ) {
        val params = recommendation.parameters
        val level = recommendation.level
        buildAnnotatedString {
            append(stringResource(R.string.sets))
            append(" *")
        }
        ParameterInputField(
            value = params.sets,
            onValueChange = {
                onRecommendationChange(
                    ExerciseEditingEvent.RecommendedSetsChanged(it, level)
                )
            },
            label = buildRequiredString(stringResource(R.string.sets)),
            modifier = Modifier.weight(1f)
        )
        ParameterInputField(
            value = params.reps,
            onValueChange = {
                onRecommendationChange(
                    ExerciseEditingEvent.RecommendedRepsChanged(it, level)
                )
            },
            label = buildRequiredString(stringResource(R.string.reps)),
            modifier = Modifier.weight(1f)
        )
        ParameterInputField(
            value = params.restTime?.inWholeSeconds?.toInt(),
            onValueChange = {
                onRecommendationChange(
                    ExerciseEditingEvent.RecommendedRestTimeChanged(it?.seconds, level)
                )
            },
            label = buildRequiredString(stringResource(R.string.rest_time)),
            unit = stringResource(R.string.second_abbrv),
            modifier = Modifier.weight(1f)
        )
        ParameterInputField(
            value = params.weightInKg,
            onValueChange = {
                onRecommendationChange(
                    ExerciseEditingEvent.RecommendedWeightChanged(it, level)
                )
            },
            label = stringResource(R.string.weight),
            unit = stringResource(R.string.kg),
            modifier = Modifier.weight(1f)
        )
    }
}

private fun buildRequiredString(str: String): String {
    return buildString {
        append(str)
        append(" *")
    }
}

@Composable
private fun RecommendedParametersCardHeader(
    isExpanded: Boolean,
    toggleExpansion: () -> Unit,
    recommendation: Recommendation,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Dimens.normal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        Box(Modifier.weight(0.1f)) {
            Box(
                modifier = Modifier
                    .size(Dimens.normal)
                    .background(recommendation.level.toColor(), shape = CircleShape)
            )
        }
        Text(
            text = stringResource(recommendation.level.nameRes),
            modifier = Modifier.weight(0.8f)
        )
        Text(
            text = if (isExpanded) "" else buildStringOverview(recommendation),
            modifier = Modifier.weight(1f)
        )
        val icon = if (isExpanded) {
            Icons.Default.ExpandLess
        } else {
            Icons.Default.ExpandMore
        }
        IconButton(onClick = toggleExpansion) {
            Icon(imageVector = icon, contentDescription = null)
        }
    }
}

private fun RecommendationLevel.toColor(): Color =
    when (this) {
        RecommendationLevel.BEGINNER -> Color.Green
        RecommendationLevel.INTERMEDIATE -> Color.Yellow
        RecommendationLevel.ADVANCED -> Color.Red
    }


@Composable
private fun buildStringOverview(recommendation: Recommendation): String =
    recommendation.parameters.let {
        if (!it.areValid()) {
            stringResource(R.string.not_set)
        } else {
            if (it.weightInKg != null) {
                stringResource(
                    R.string.recommendation_overview,
                    it.sets!!,
                    it.reps!!,
                    it.restTime!!.inWholeSeconds,
                    it.weightInKg!!
                )
            } else {
                stringResource(
                    R.string.recommendation_no_weight,
                    it.sets!!,
                    it.reps!!,
                    it.restTime!!.inWholeSeconds,
                )
            }
        }
    }

@Composable
private fun ParameterInputField(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    unit: String = "",
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            modifier = Modifier.padding(bottom = Dimens.normal),
            style = MaterialTheme.typography.labelMedium,
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
            modifier = Modifier.defaultMinSize(minWidth = 40.dp),
            suffix = { Text(unit) }
        )
    }
}

@Composable
private fun RecommendedParametersExtendedSummary(
    params: RecommendedParameters,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ParameterOverview(
            value = params.sets,
            label = "Sets:"
        )
        ParameterOverview(
            value = params.reps,
            label = "Reps:"
        )
        ParameterOverview(
            value = params.restTime?.inWholeSeconds?.toInt(),
            label = "Rest time:",
            unit = stringResource(R.string.second_abbrv)
        )
        ParameterOverview(
            value = params.weightInKg,
            label = "Weight:",
            unit = stringResource(R.string.kg)
        )

    }
}

@Composable
private fun ParameterOverview(
    value: Int?,
    label: String,
    modifier: Modifier = Modifier,
    unit: String = "",
) {
    if (value == null) return
    Row(
        modifier,
    ) {
        Text(text = label)
        Spacer(Modifier.width(Dimens.small))
        Text(text = value.toString())
        Text(text = unit)
    }
}

@Preview
@Composable
private fun RecommendedParametersFormPreview() {
    TrainingPlannerTheme {
        RecommendedParametersForm(
            recommendations = PREVIEW_RECOMMENDED_PARAMETERS,
            onRecommendationChange = {}
        )
    }
}

@Preview
@Composable
private fun RecommendedParametersOverviewPreview() {
    TrainingPlannerTheme {
        RecommendedParametersOverview(
            recommendations = PREVIEW_RECOMMENDED_PARAMETERS,
            modifier = Modifier,
        )
    }
}

val PREVIEW_RECOMMENDED_PARAMETERS = listOf(
    Recommendation(
        RecommendationLevel.BEGINNER, parameters = RecommendedParameters(
            sets = 3,
            reps = 10,
            restTime = 3.minutes
        )
    ),
    Recommendation(
        RecommendationLevel.INTERMEDIATE, parameters = RecommendedParameters(
            sets = 3,
            reps = 10,
            restTime = 1.minutes,
            weightInKg = 30
        )
    ),
    Recommendation(RecommendationLevel.ADVANCED),
)