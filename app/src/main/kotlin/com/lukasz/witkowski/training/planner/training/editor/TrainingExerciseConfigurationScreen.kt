package com.lukasz.witkowski.training.planner.training.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseParametersInputFields
import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseParametersIntent
import com.lukasz.witkowski.training.planner.exercise.createExercise.PREVIEW_RECOMMENDED_PARAMETERS
import com.lukasz.witkowski.training.planner.exercise.createExercise.RecommendedParametersCardHeader
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsScreenContent
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsState
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise2
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.ui.components.ConfirmButton
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun TrainingExerciseConfigurationScreen(
    viewModel: TrainingExerciseConfigurationViewModel,
    onExerciseConfigured: (TrainingExercise) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val parameters by viewModel.parameters.collectAsState()
    TrainingExerciseConfigurationScreenContent(
        state = state,
        parameters = parameters,
        modifier = modifier.fillMaxSize(),
        onParametersIntent = viewModel::processParametersIntent,
        onRecommendationSelected = viewModel::recommendationSelected,
        onAddTrainingExercise = {
            val trainingExercise = viewModel.createTrainingExercise()
            onExerciseConfigured(trainingExercise)
        }
    )

}

@Composable
private fun TrainingExerciseConfigurationScreenContent(
    state: ExerciseDetailsState,
    parameters: RecommendedParameters,
    onParametersIntent: (ExerciseParametersIntent) -> Unit,
    onRecommendationSelected: (RecommendedParameters) -> Unit,
    onAddTrainingExercise: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExerciseDetailsScreenContent(
        state = state,
        modifier = modifier,
        footer = { exerciseDetails ->
            TrainingExerciseParameters(
                details = exerciseDetails,
                onAddTrainingExercise = onAddTrainingExercise,
                parameters = parameters,
                onParametersIntent = onParametersIntent,
                onRecommendationSelected = onRecommendationSelected
            )

        }
    )
}

@Composable
private fun TrainingExerciseParameters(
    details: ExerciseDetails,
    onAddTrainingExercise: () -> Unit,
    parameters: RecommendedParameters,
    onParametersIntent: (ExerciseParametersIntent) -> Unit,
    onRecommendationSelected: (RecommendedParameters) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        Text(
            text = stringResource(R.string.recommended_parameters_label),
            style = MaterialTheme.typography.titleLarge
        )
        details.recommendations.forEach {
            val isSelected = parameters == it.parameters
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Unspecified
                ),
                modifier = Modifier.clickable {
                    onRecommendationSelected(it.parameters)
                }
            ) {
                RecommendedParametersCardHeader(
                    isExpandable = false,
                    isExpanded = false,
                    recommendation = it,
                    toggleExpansion = {},
                    modifier = Modifier.padding(Dimens.large)
                )
            }
        }

        Text(
            text = stringResource(R.string.set_parameters),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.set_parameters_description),
            style = MaterialTheme.typography.bodyMedium
        )
        ExerciseParametersInputFields(
            parameters = parameters,
            testTagPrefix = "Config",
            onParametersIntent = onParametersIntent
        )
        ConfirmButton(
            text = stringResource(R.string.add_exercise),
            onClick = onAddTrainingExercise,
            isEnabled = parameters.areValid()
        )
    }

}

@Preview
@Composable
private fun TrainingExerciseConfigurationScreenContentPreview() {
    TrainingPlannerTheme {
        Surface {
            TrainingExerciseConfigurationScreenContent(
                state = PREVIEW_SUCCESS_STATE,
                modifier = Modifier.fillMaxSize(),
                parameters = RecommendedParameters(),
                onParametersIntent = {},
                onRecommendationSelected = {},
                onAddTrainingExercise = {}
            )
        }
    }
}

@Preview
@Composable
private fun TrainingExerciseConfigurationScreenContentSelectedRecommendationPreview() {
    TrainingPlannerTheme {
        Surface {
            TrainingExerciseConfigurationScreenContent(
                state = PREVIEW_SUCCESS_STATE,
                modifier = Modifier.fillMaxSize(),
                parameters = PREVIEW_RECOMMENDED_PARAMETERS.first().parameters,
                onParametersIntent = {},
                onRecommendationSelected = {},
                onAddTrainingExercise = {}
            )
        }
    }
}

private val PREVIEW_SUCCESS_STATE = ExerciseDetailsState.Success(
    details = ExerciseDetails(
        exercise = Exercise2(
            id = ExerciseId.create(),
            name = "Push ups",
            description = "Some long description in here",
            categories = listOf(
                ExerciseCategory("Chest"),
                ExerciseCategory("Shoulder")
            )
        ),
        recommendations = PREVIEW_RECOMMENDED_PARAMETERS
    ),
)