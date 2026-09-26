package com.lukasz.witkowski.training.planner.exercise.createExercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.components.FormFieldLabel
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun RecommendedParametersForm(
    recommendations: List<Recommendation>,
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
            RecommendedParametersCard(it)
        }

    }
}

@Composable
private fun RecommendedParametersCard(
    recommendation: Recommendation,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.normal)
                    .background(recommendation.level.color, shape = CircleShape)
            )
            Text(stringResource(recommendation.level.nameRes))
            val overview = buildStringOverview(recommendation)
            Text(overview)
        }
    }

}

@Composable
private fun buildStringOverview(recommendation: Recommendation): String = recommendation.parameters?.let {
    if (it.weightInKg != null) {
        stringResource(
            R.string.recommendation_overview,
            it.sets,
            it.reps,
            it.restTime.inWholeSeconds,
            it.weightInKg
        )
    } else {
        stringResource(
            R.string.recommendation_no_weight,
            it.sets,
            it.reps,
            it.restTime.inWholeSeconds,
        )
    }
} ?: stringResource(R.string.not_set)

@Preview
@Composable
private fun RecommendedParametersFormPreview() {
    TrainingPlannerTheme {
        RecommendedParametersForm(
            recommendations = PREVIEW_RECOMMENDED_PARAMETERS
        )
    }
}

val PREVIEW_RECOMMENDED_PARAMETERS = listOf(
    Recommendation(RecommendationLevel.BEGINNER),
    Recommendation(RecommendationLevel.INTERMEDIATE),
    Recommendation(RecommendationLevel.ADVANCED),
)