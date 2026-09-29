package com.lukasz.witkowski.training.planner.exercise.exercisesList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise2
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.CategoryFilters
import com.lukasz.witkowski.training.planner.ui.components.Image
import com.lukasz.witkowski.training.planner.ui.components.ImageContainer
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.NoDataMessage
import com.lukasz.witkowski.training.planner.ui.components.PREVIEW_CATEGORIES
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun ExercisesScreen(
    modifier: Modifier = Modifier,
    viewModel: ExercisesListViewModel,
    onExerciseClicked: (ExerciseId) -> Unit
) {
    val exercisesList = viewModel.exercises.collectAsLazyPagingItems()
    val filteringState by viewModel.filteringState.collectAsState()
    ExercisesScreenContent(
        modifier = modifier.fillMaxSize(),
        exercisesList = exercisesList,
        filteringState = filteringState,
        toggleCategory = { viewModel.toggleCategory(it) },
        onSearchQueryChanged = viewModel::onSearchQueryChange,
        onExerciseClicked = onExerciseClicked
    )
}

@Composable
private fun ExercisesScreenContent(
    exercisesList: LazyPagingItems<Exercise2>,
    filteringState: FilteringState,
    toggleCategory: (ExerciseCategory) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onExerciseClicked: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TextField(
            text = filteringState.searchQuery,
            onTextChange = onSearchQueryChanged,
            label = stringResource(R.string.search_exercise),
            modifier = Modifier.padding(Dimens.normal)
        )
        CategoryFilters(
            modifier = Modifier.padding(
                bottom = Dimens.normal,
                start = Dimens.normal,
                end = Dimens.normal
            ),
            categories = filteringState.categories,
            toggleCategory = toggleCategory
        )
        if (exercisesList.itemCount != 0) {
            ExercisesList(
                exercisesList = exercisesList,
                onExerciseClicked = onExerciseClicked,
            )
        } else {
            NoDataMessage(
                text = if (filteringState.isAnyCategorySelected) {
                    stringResource(id = R.string.no_exercises_for_categories)
                } else {
                    stringResource(id = R.string.no_exercises)
                }
            )
        }
    }
}

@Composable
private fun ExercisesList(
    modifier: Modifier = Modifier,
    exercisesList: LazyPagingItems<Exercise2>,
    onExerciseClicked: (ExerciseId) -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LazyColumn {
            items(
                count = exercisesList.itemCount,
                key = exercisesList.itemKey { it.id.value.toString() }
            ) { index ->
                val exercise = exercisesList[index]
                if (exercise != null) {
                    ExerciseListItemContent(
                        exercise = exercise,
                        onClick = { onExerciseClicked(exercise.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseListItemContent(
    exercise: Exercise2,
    onClick: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageDescription = stringResource(id = R.string.image_description, exercise.name)
    ListCardItem(
        onCardClicked = {
            onClick(exercise.id)
        },
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageWithDefaultPlaceholder(
                imageDescription = imageDescription,
                imageReference = exercise.image
            )
            Spacer(modifier = Modifier.width(Dimens.large))
            ExerciseInformation(
                modifier = Modifier,
                exercise = exercise,
            )
        }
    }
}

@Composable
private fun ExerciseInformation(
    modifier: Modifier = Modifier,
    exercise: Exercise2,
) {
    val categories = exercise.categories
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.titleLarge
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.normal)
        ) {
            categories.forEach { category ->
                CategoryChip(
                    modifier = Modifier,
                    category = category,
                )
            }
        }
    }
}

@Composable
private fun ImageWithDefaultPlaceholder(
    modifier: Modifier = Modifier,
    imageDescription: String,
    imageReference: ImageReference?,
    size: Dp = 60.dp,
) {
    ImageContainer(modifier = modifier.size(size)) {
        Image(
            modifier = Modifier.fillMaxSize(),
            imageReference = imageReference,
            defaultImage = R.drawable.exercise_default,
            contentDescriptor = imageDescription
        )
    }
}

fun <T : Any> List<T>.asPreviewPagerFlow(): Flow<PagingData<T>> {
    return flowOf(PagingData.from(this))
}

@Preview
@Composable
private fun ExerciseListItemPreview() {
    TrainingPlannerTheme {
        ExerciseListItemContent(
            exercise = PREVIEW_EXERCISES.first(),
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun ExercisesScreenContentPreview() {
    TrainingPlannerTheme {
        ExercisesScreenContent(
            exercisesList = PREVIEW_EXERCISES.asPreviewPagerFlow().collectAsLazyPagingItems(),
            filteringState = FilteringState(
                searchQuery = "",
                categories = PREVIEW_CATEGORIES
            ),
            toggleCategory = {},
            onSearchQueryChanged = {},
            onExerciseClicked = { },
        )
    }
}

val PREVIEW_EXERCISES = listOf(
    Exercise2(
        id = ExerciseId.create(),
        name = "Push ups",
        description = "Some long description in here",
        categories = listOf(ExerciseCategory("Chest"), ExerciseCategory("Arm"))
    ),
    Exercise2(
        id = ExerciseId.create(),
        name = "Pull up",
        description = "Some long description in here",
        categories = listOf(ExerciseCategory("Back"), ExerciseCategory("Arm"))
    ),
)



