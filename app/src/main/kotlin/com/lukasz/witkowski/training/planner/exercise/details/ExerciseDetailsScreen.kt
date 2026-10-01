package com.lukasz.witkowski.training.planner.exercise.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.createExercise.ImagesPreview
import com.lukasz.witkowski.training.planner.exercise.createExercise.PREVIEW_RECOMMENDED_PARAMETERS
import com.lukasz.witkowski.training.planner.exercise.createExercise.RecommendedParametersOverview
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise2
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseDetails
import com.lukasz.witkowski.training.planner.ui.components.CategoryChips
import com.lukasz.witkowski.training.planner.ui.components.FailureScreen
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun ExerciseDetailsScreen(
    viewModel: ExerciseDetailsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    ExerciseDetailsScreenContent(
        state = state,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun ExerciseDetailsScreenContent(
    state: ExerciseDetailsState,
    modifier: Modifier = Modifier
) {
    when (state) {
        ExerciseDetailsState.Loading -> {
            LoadingScreen(
                modifier = modifier,
                message = stringResource(R.string.loading_exercise_details)
            )
        }

        is ExerciseDetailsState.Failure -> {
            FailureScreen(
                modifier = modifier,
                title = stringResource(R.string.failed_load_exercise_details),
                message = state.message
            )
        }

        is ExerciseDetailsState.Success -> {
            ExerciseDetails(
                details = state.details,
                modifier = modifier
            )
        }
    }
}


@Composable
private fun ExerciseDetails(
    details: ExerciseDetails,
    modifier: Modifier = Modifier
) {
    val exercise = details.exercise
    Column(
        modifier.padding(Dimens.normal),
        verticalArrangement = Arrangement.spacedBy(Dimens.large)
    ) {
        ImagesPreview(
            images = emptyList(),
            previewImage = null,
            modifier = Modifier.padding(bottom = Dimens.large)
        )
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.headlineSmall,
        )
        CategoryChips(exercise.categories)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.normal)) {
            Text(
                text = stringResource(R.string.description),
                style = MaterialTheme.typography.titleLarge
            )
            Text(text = exercise.description)
        }
        RecommendedParametersOverview(
            recommendations = details.recommendations
        )
    }
}

@Composable
@Preview
private fun ExerciseDetailsScreenContentPreview() {
    TrainingPlannerTheme {
        Surface {
            ExerciseDetailsScreenContent(
                state = ExerciseDetailsState.Success(
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
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@Preview
private fun ExerciseDetailsScreenLoadingPreview() {
    TrainingPlannerTheme {
        ExerciseDetailsScreenContent(
            state = ExerciseDetailsState.Loading,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
@Preview
private fun ExerciseDetailsScreenFailurePreview() {
    TrainingPlannerTheme {
        ExerciseDetailsScreenContent(
            state = ExerciseDetailsState.Failure("No exercise in DB"),
            modifier = Modifier.fillMaxSize()
        )
    }
}