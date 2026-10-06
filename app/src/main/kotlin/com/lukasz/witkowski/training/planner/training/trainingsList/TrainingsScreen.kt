package com.lukasz.witkowski.training.planner.training.trainingsList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanOverview
import kotlin.time.Instant
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.FailureScreen
import com.lukasz.witkowski.training.planner.ui.components.FilteringState
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen
import com.lukasz.witkowski.training.planner.ui.components.NoDataMessage
import com.lukasz.witkowski.training.planner.ui.components.SearchHeader
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun TrainingsScreen(
    viewModel: TrainingsListViewModel,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    TrainingsScreenContent(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onTrainingPlanClicked = onTrainingPlanClicked,
        onSearchQueryChanged = viewModel::onSearchQueryChange,
        toggleCategory = viewModel::toggleCategory
    )
}

@Composable
private fun TrainingsScreenContent(
    modifier: Modifier = Modifier,
    uiState: TrainingsListUiState,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    toggleCategory: (ExerciseCategory) -> Unit
) {
    Column(modifier) {
        val filteringState = uiState.filteringState
        SearchHeader(
            query = filteringState.searchQuery,
            onSearchQueryChanged = onSearchQueryChanged,
            categories = filteringState.categories,
            toggleCategory = toggleCategory,
            label = stringResource(R.string.search_training_plan)
        )
        when (uiState) {
            is TrainingsListUiState.Loading -> LoadingScreen(
                modifier = Modifier.fillMaxSize(),
            )
            is TrainingsListUiState.Failure -> FailureScreen(
                modifier = Modifier.fillMaxSize(),
                title = stringResource(R.string.failed_to_load_training_plans),
                message = uiState.message ?: ""
            )
            is TrainingsListUiState.Success -> TrainingPlansOverviews(
                plans = uiState.plans,
                onTrainingPlanClicked = onTrainingPlanClicked
            )
        }
    }
}

@Composable
private fun TrainingPlansOverviews(
    modifier: Modifier = Modifier,
    plans: List<TrainingPlanOverview>,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
) {
    if (plans.isNotEmpty()) {
        TrainingsList(
            modifier = modifier,
            plans = plans,
            onTrainingPlanClicked = onTrainingPlanClicked
        )
    } else {
        NoDataMessage(
            modifier = modifier,
            text = stringResource(id = R.string.no_trainings_info)
        )
    }
}


@Composable
private fun TrainingsList(
    modifier: Modifier = Modifier,
    plans: List<TrainingPlanOverview>,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(plans, key = { it.id.toString() }) { trainingOverview ->
            ListCardItem(modifier = Modifier,
                onCardClicked = { onTrainingPlanClicked(trainingOverview.id) }) {
                TrainingListItemContent(
                    trainingOverview = trainingOverview,
                )
            }
        }
        item { Spacer(modifier = Modifier.height(74.dp)) }
    }
}

@Composable
fun TrainingListItemContent(
    modifier: Modifier = Modifier,
    trainingOverview: TrainingPlanOverview,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f)
        ) {
            Text(
                text = trainingOverview.title,
                fontSize = 28.sp
            )
//            CategoriesRow(
//                modifier = modifier.padding(top = 16.dp),
//                categories = trainingOverview.getCategories()
//            )
        }
        Icon(
            modifier = Modifier
                .size(40.dp),
//                .clickable { startTrainingSession(trainingPlan) },
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = stringResource(id = R.string.start_training_session),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun CategoriesRow(
    modifier: Modifier = Modifier,
    categories: List<Category>
) {
    LazyRow(modifier = modifier) {
        items(categories) { item: Category ->
            CategoryChip(
                modifier = Modifier.padding(end = 8.dp),
                category = item,
            )
        }
    }
}

@Preview
@Composable
private fun TrainingsScreenLoadingPreview() {
    TrainingPlannerTheme {
        TrainingsScreenContent(
            uiState = TrainingsListUiState.Loading(
                filteringState = FilteringState("Some query", categories = emptyList())
            ),
            onTrainingPlanClicked = {},
            onSearchQueryChanged = {},
            toggleCategory = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun TrainingsScreenFailurePreview() {
    TrainingPlannerTheme {
        TrainingsScreenContent(
            uiState = TrainingsListUiState.Failure(
                filteringState = FilteringState("Some query", categories = emptyList()),
                message = "Detailed failure message"
            ),
            onTrainingPlanClicked = {},
            onSearchQueryChanged = {},
            toggleCategory = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun TrainingsScreenEmptyListPreview() {
    TrainingPlannerTheme {
        TrainingsScreenContent(
            uiState = TrainingsListUiState.Success(
                filteringState = FilteringState("Some query", categories = emptyList()),
                plans = emptyList()
            ),
            onTrainingPlanClicked = {},
            onSearchQueryChanged = {},
            toggleCategory = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun TrainingsScreenSuccessPreview() {
    TrainingPlannerTheme {
        TrainingsScreenContent(
            uiState = TrainingsListUiState.Success(
                filteringState = FilteringState("Some query", categories = emptyList()),
                plans = PREVIEW_TRAINING_PLANS
            ),
            onTrainingPlanClicked = {},
            onSearchQueryChanged = {},
            toggleCategory = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

private val PREVIEW_TRAINING_PLANS = listOf(
    TrainingPlanOverview(
        id = TrainingPlanId.create(),
        title = "Full Body Workout",
        description = "A complete full body workout targeting all major muscle groups",
        categories = setOf(
            ExerciseCategoryName("Chest"),
            ExerciseCategoryName("Back"),
            ExerciseCategoryName("Legs")
        ),
        lastModification = Instant.parse("2026-09-01T12:00:00Z"),
        lastSession = Instant.parse("2026-09-10T14:30:00Z")
    ),
    TrainingPlanOverview(
        id = TrainingPlanId.create(),
        title = "Upper Body Strength",
        description = "Focus on chest, back, shoulders and arms strength",
        categories = setOf(
            ExerciseCategoryName("Chest"),
            ExerciseCategoryName("Back"),
            ExerciseCategoryName("Shoulders"),
            ExerciseCategoryName("Arms")
        ),
        lastModification = Instant.parse("2026-09-05T10:00:00Z"),
        lastSession = null
    ),
    TrainingPlanOverview(
        id = TrainingPlanId.create(),
        title = "Cardio & Core",
        description = "High intensity cardio routine with core stability exercises",
        categories = setOf(
            ExerciseCategoryName("Cardio"),
            ExerciseCategoryName("Abs")
        ),
        lastModification = Instant.parse("2026-09-08T08:15:00Z"),
        lastSession = Instant.parse("2026-09-12T18:00:00Z")
    )
)


