package com.lukasz.witkowski.training.planner.analytics.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.lukasz.witkowski.training.planner.analytics.R
import com.lukasz.witkowski.training.planner.analytics.domain.models.ExerciseStatisticsOverview
import com.lukasz.witkowski.training.planner.analytics.domain.models.TrainingStatisticsOverview
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppEmptyState
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun TrainingStatisticsHost(
    viewModel: TrainingStatisticsViewModel,
    content: @Composable (LazyListScope.() -> Unit) -> Unit
) {
    val overviews = viewModel.statisticsOverviews.collectAsLazyPagingItems()
    val detailsUiState by viewModel.statisticsDetails.collectAsStateWithLifecycle()

    content {
        trainingStatisticsItems(
            overviews = overviews,
            onSessionClicked = viewModel::onSessionClicked
        )
    }

    val currentDetailsState = detailsUiState
    if (currentDetailsState is DetailsUiState.Success) {
        TrainingStatisticsDetailsSheet(
            statistics = currentDetailsState.details,
            onDismissRequest = viewModel::onSessionDismissed
        )
    }
}

fun LazyListScope.trainingStatisticsItems(
    overviews: LazyPagingItems<TrainingStatisticsOverview>,
    onSessionClicked: (TrainingStatisticsId) -> Unit
) {
    if (overviews.itemCount == 0) {
        item(key = "empty_previous_sessions") {
            AppEmptyState(
                title = stringResource(id = R.string.no_previous_sessions),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.spaceXl)
            )
        }
    } else {
        val isEndReached = overviews.loadState.append.endOfPaginationReached
        items(
            count = overviews.itemCount,
            key = overviews.itemKey { it.id.value }
        ) { index ->
            overviews[index]?.let { overview ->
                StatisticOverviewItem(
                    overview = overview,
                    isFirst = index == 0,
                    isLast = index == overviews.itemCount - 1 && isEndReached,
                    onItemClicked = { onSessionClicked(overview.id) }
                )
            }
        }
    }
}

@Composable
private fun TrainingStatisticsTabContent(
    overviews: LazyPagingItems<TrainingStatisticsOverview>,
    detailsUiState: DetailsUiState,
    onSessionClicked: (TrainingStatisticsId) -> Unit,
    onDetailsDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        trainingStatisticsItems(
            overviews = overviews,
            onSessionClicked = onSessionClicked
        )
    }
    if (detailsUiState is DetailsUiState.Success) {
        TrainingStatisticsDetailsSheet(
            statistics = detailsUiState.details,
            onDismissRequest = onDetailsDismiss
        )
    }
}

private fun <T : Any> List<T>.asPreviewPagerFlow(): Flow<PagingData<T>> =
    flowOf(PagingData.from(this))

private val PREVIEW_EXERCISE_STATISTICS_OVERVIEWS = listOf(
    ExerciseStatisticsOverview(
        id = ExerciseStatisticsId.create(),
        trainingExerciseId = TrainingExerciseId.create(),
        exerciseName = "Barbell Bench Press",
        totalTime = 15.minutes,
        completedSetsCount = 4,
        totalSetsCount = 4
    ),
    ExerciseStatisticsOverview(
        id = ExerciseStatisticsId.create(),
        trainingExerciseId = TrainingExerciseId.create(),
        exerciseName = "Incline Dumbbell Press",
        totalTime = 12.minutes,
        completedSetsCount = 3,
        totalSetsCount = 4
    ),
    ExerciseStatisticsOverview(
        id = ExerciseStatisticsId.create(),
        trainingExerciseId = TrainingExerciseId.create(),
        exerciseName = "Push Ups",
        totalTime = 8.minutes,
        completedSetsCount = 3,
        totalSetsCount = 3
    )
)

internal val PREVIEW_TRAINING_STATISTICS_OVERVIEWS = listOf(
    TrainingStatisticsOverview(
        id = TrainingStatisticsId.create(),
        trainingPlanId = TrainingPlanId.create(),
        date = Instant.parse("2026-09-10T12:00:00Z"),
        totalTime = 45.minutes,
        totalVolumeKg = 0.0,
        completedSetsCount = 10,
        totalSetsCount = 11,
        rpe = SessionRpe.HARD,
        note = "Great chest & triceps workout. Pushed hard on bench press.",
        exerciseOverviews = PREVIEW_EXERCISE_STATISTICS_OVERVIEWS
    ),
    TrainingStatisticsOverview(
        id = TrainingStatisticsId.create(),
        trainingPlanId = TrainingPlanId.create(),
        date = Instant.parse("2026-09-08T12:00:00Z"),
        totalTime = 52.minutes,
        totalVolumeKg = 4100.0,
        completedSetsCount = 12,
        totalSetsCount = 12,
        rpe = SessionRpe.CHALLENGING,
        note = null,
        exerciseOverviews = listOf(
            ExerciseStatisticsOverview(
                id = ExerciseStatisticsId.create(),
                trainingExerciseId = TrainingExerciseId.create(),
                exerciseName = "Barbell Squat",
                totalTime = 20.minutes,
                completedSetsCount = 4,
                totalSetsCount = 4
            ),
            ExerciseStatisticsOverview(
                id = ExerciseStatisticsId.create(),
                trainingExerciseId = TrainingExerciseId.create(),
                exerciseName = "Romanian Deadlift",
                totalTime = 15.minutes,
                completedSetsCount = 4,
                totalSetsCount = 4
            )
        )
    )
)

private val PREVIEW_TRAINING_EXERCISE_ID = TrainingExerciseId.create()

private val PREVIEW_EXERCISE_ATTEMPT_STATISTICS = listOf(
    ExerciseAttemptStatistics(
        id = ExerciseAttemptStatisticsId.create(),
        trainingExerciseId = PREVIEW_TRAINING_EXERCISE_ID,
        set = 1,
        completed = true,
        actualReps = 10,
        actualWeightInKg = 80.0,
        actualTime = 45.seconds,
        effort = SetEffort.MODERATE,
        restTime = 90.seconds,
        plannedRestTime = 90.seconds,
        time = 54.seconds
    ),
    ExerciseAttemptStatistics(
        id = ExerciseAttemptStatisticsId.create(),
        trainingExerciseId = PREVIEW_TRAINING_EXERCISE_ID,
        set = 2,
        completed = true,
        actualReps = 9,
        actualWeightInKg = 80.0,
        actualTime = 42.seconds,
        effort = SetEffort.HARD,
        restTime = 90.seconds,
        plannedRestTime = 90.seconds,
        time = 62.seconds
    ),
    ExerciseAttemptStatistics(
        id = ExerciseAttemptStatisticsId.create(),
        trainingExerciseId = PREVIEW_TRAINING_EXERCISE_ID,
        set = 3,
        completed = true,
        actualReps = 6,
        actualWeightInKg = 80.0,
        actualTime = 30.seconds,
        effort = SetEffort.MAXIMAL,
        restTime = 120.seconds,
        plannedRestTime = 90.seconds,
        time = 74.seconds
    )
)

private val PREVIEW_EXERCISE_STATISTICS = listOf(
    ExerciseStatistics(
        id = ExerciseStatisticsId.create(),
        trainingExerciseId = PREVIEW_TRAINING_EXERCISE_ID,
        exerciseName = "Barbell Bench Press longer name",
        plannedSets = 3,
        plannedConfig = SessionExerciseConfiguration(
            reps = 10,
            weightInKg = 80.0,
            holdTime = 45.seconds
        ),
        plannedRestTime = 90.seconds,
        attemptsStatistics = PREVIEW_EXERCISE_ATTEMPT_STATISTICS
    ),
    ExerciseStatistics(
        id = ExerciseStatisticsId.create(),
        trainingExerciseId = TrainingExerciseId.create(),
        exerciseName = "Push Ups",
        plannedSets = 2,
        plannedConfig = SessionExerciseConfiguration(
            reps = 15,
            weightInKg = null,
            holdTime = null
        ),
        plannedRestTime = 60.seconds,
        attemptsStatistics = listOf(
            ExerciseAttemptStatistics(
                id = ExerciseAttemptStatisticsId.create(),
                trainingExerciseId = TrainingExerciseId.create(),
                set = 1,
                completed = true,
                actualReps = 15,
                actualWeightInKg = null,
                actualTime = null,
                effort = SetEffort.MODERATE,
                restTime = 60.seconds,
                plannedRestTime = 60.seconds,
                time = 123.seconds
            )
        )
    )
)

internal val PREVIEW_TRAINING_STATISTICS = TrainingStatistics(
    id = TrainingStatisticsId.create(),
    trainingPlanId = TrainingPlanId.create(),
    totalTime = 45.minutes,
    date = Instant.parse("2026-09-10T12:00:00Z"),
    exercisesStatistics = PREVIEW_EXERCISE_STATISTICS,
    note = "Great chest & triceps workout. Pushed hard on bench press.",
    rpe = SessionRpe.HARD
)

private val PREVIEW_DETAILS_SUCCESS_STATE = DetailsUiState.Success(PREVIEW_TRAINING_STATISTICS)

@AppPreview
@Composable
private fun TrainingStatisticsTabPreview() {
    TrainingPlannerTheme {
        TrainingStatisticsTabContent(
            overviews = PREVIEW_TRAINING_STATISTICS_OVERVIEWS.asPreviewPagerFlow()
                .collectAsLazyPagingItems(),
            detailsUiState = DetailsUiState.NotSelected,
            onSessionClicked = {},
            onDetailsDismiss = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@AppPreview
@Composable
private fun TrainingStatisticsTabDetailsPreview() {
    TrainingPlannerTheme {
        TrainingStatisticsTabContent(
            overviews = PREVIEW_TRAINING_STATISTICS_OVERVIEWS.asPreviewPagerFlow()
                .collectAsLazyPagingItems(),
            detailsUiState = PREVIEW_DETAILS_SUCCESS_STATE,
            onSessionClicked = {},
            onDetailsDismiss = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@AppPreview
@Composable
private fun TrainingStatisticsTabEmptyPreview() {
    TrainingPlannerTheme {
        TrainingStatisticsTabContent(
            overviews = emptyList<TrainingStatisticsOverview>().asPreviewPagerFlow()
                .collectAsLazyPagingItems(),
            detailsUiState = DetailsUiState.NotSelected,
            onSessionClicked = {},
            onDetailsDismiss = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
