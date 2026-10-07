package com.lukasz.witkowski.training.planner.training.editor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesScreenContent
import com.lukasz.witkowski.training.planner.exercise.exercisesList.PREVIEW_EXERCISES
import com.lukasz.witkowski.training.planner.exercise.exercisesList.asPreviewPagerFlow
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise2
import com.lukasz.witkowski.training.planner.ui.components.FilteringState
import com.lukasz.witkowski.training.planner.ui.components.PREVIEW_CATEGORIES
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun AddTrainingExerciseScreen(
    viewModel: ExercisesListViewModel,
    onExerciseSelected: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val exercisesList = viewModel.exercises.collectAsLazyPagingItems()
    val filteringState by viewModel.filteringState.collectAsState()
    AddTrainingExerciseScreenContent(
        modifier = modifier.fillMaxSize(),
        exercisesList = exercisesList,
        filteringState = filteringState,
        toggleCategory = { viewModel.toggleCategory(it) },
        onSearchQueryChanged = viewModel::onSearchQueryChange,
        onExerciseSelected = onExerciseSelected
    )
}

@Composable
private fun AddTrainingExerciseScreenContent(
    onExerciseSelected: (ExerciseId) -> Unit,
    exercisesList: LazyPagingItems<Exercise2>,
    filteringState: FilteringState,
    toggleCategory: (ExerciseCategory) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExercisesScreenContent(
        modifier = modifier,
        exercisesList = exercisesList,
        filteringState = filteringState,
        toggleCategory = toggleCategory,
        onSearchQueryChanged = onSearchQueryChanged,
        onExerciseClicked = { onExerciseSelected(it) },
        itemTrailingIcon = {
            IconButton(
                onClick = { onExerciseSelected(it) },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_exercise_icon),
                    modifier = Modifier.padding(Dimens.normal)
                )
            }
        }
    )
}

@Preview
@Composable
private fun AddTrainingExerciseScreenContentPreview() {
    TrainingPlannerTheme {
        AddTrainingExerciseScreenContent(
            exercisesList = PREVIEW_EXERCISES.asPreviewPagerFlow().collectAsLazyPagingItems(),
            filteringState = FilteringState(
                searchQuery = "",
                categories = PREVIEW_CATEGORIES
            ),
            toggleCategory = {},
            onSearchQueryChanged = {},
            onExerciseSelected = {}
        )
    }
}