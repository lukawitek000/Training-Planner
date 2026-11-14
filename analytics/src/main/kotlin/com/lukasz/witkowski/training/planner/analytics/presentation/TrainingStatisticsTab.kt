package com.lukasz.witkowski.training.planner.analytics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.ShapeFull
import com.lukasz.witkowski.training.planner.shared.theme.Success
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.theme.Warning
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppEmptyState
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun TrainingStatisticsTab(
    viewModel: TrainingStatisticsViewModel,
    modifier: Modifier = Modifier,
) {
    val overviews = viewModel.statisticsOverviews.collectAsLazyPagingItems()
    val detailsUiState by viewModel.statisticsDetails.collectAsStateWithLifecycle()
    TrainingStatisticsTabContent(
        overviews = overviews,
        detailsUiState = detailsUiState,
        onSessionClicked = viewModel::onSessionClicked,
        onDetailsDismiss = viewModel::onSessionDismissed,
        modifier = modifier,
    )
}

@Composable
private fun TrainingStatisticsTabContent(
    overviews: LazyPagingItems<TrainingStatisticsOverview>,
    detailsUiState: DetailsUiState,
    onSessionClicked: (TrainingStatisticsId) -> Unit,
    onDetailsDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (overviews.itemCount == 0) {
        AppEmptyState(
            title = stringResource(id = R.string.no_previous_sessions),
            modifier = modifier
        )
    } else {
        StatisticsOverviewsList(
            overviews = overviews,
            onSessionClicked = onSessionClicked,
            modifier = modifier
        )
    }
}

@Composable
private fun StatisticsOverviewsList(
    overviews: LazyPagingItems<TrainingStatisticsOverview>,
    onSessionClicked: (TrainingStatisticsId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
    ) {
        items(
            count = overviews.itemCount,
            key = overviews.itemKey { it.id.value }
        ) { index ->
            overviews[index]?.let {
                StatisticOverviewItem(
                    overview = it,
                    onItemClicked = { onSessionClicked(it.id) }
                )
            }
        }
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
        date = Date(),
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
        date = Date(System.currentTimeMillis() - (86400000L * 2)),
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
        restTime = 90.seconds
    ),
    ExerciseAttemptStatistics(
        id = ExerciseAttemptStatisticsId.create(),
        trainingExerciseId = PREVIEW_TRAINING_EXERCISE_ID,
        set = 2,
        completed = true,
        actualReps = 10,
        actualWeightInKg = 80.0,
        actualTime = 48.seconds,
        effort = SetEffort.HARD,
        restTime = 90.seconds
    ),
    ExerciseAttemptStatistics(
        id = ExerciseAttemptStatisticsId.create(),
        trainingExerciseId = PREVIEW_TRAINING_EXERCISE_ID,
        set = 3,
        completed = true,
        actualReps = 8,
        actualWeightInKg = 80.0,
        actualTime = 50.seconds,
        effort = SetEffort.MAXIMAL,
        restTime = 120.seconds
    )
)

private val PREVIEW_EXERCISE_STATISTICS = listOf(
    ExerciseStatistics(
        id = ExerciseStatisticsId.create(),
        trainingExerciseId = PREVIEW_TRAINING_EXERCISE_ID,
        exerciseName = "Barbell Bench Press",
        attemptsStatistics = PREVIEW_EXERCISE_ATTEMPT_STATISTICS
    ),
    ExerciseStatistics(
        id = ExerciseStatisticsId.create(),
        trainingExerciseId = TrainingExerciseId.create(),
        exerciseName = "Push Ups",
        attemptsStatistics = listOf(
            ExerciseAttemptStatistics(
                id = ExerciseAttemptStatisticsId.create(),
                trainingExerciseId = TrainingExerciseId.create(),
                set = 1,
                completed = true,
                actualReps = 15,
                actualWeightInKg = null,
                actualTime = 30.seconds,
                effort = SetEffort.MODERATE,
                restTime = 60.seconds
            )
        )
    )
)

private val PREVIEW_TRAINING_STATISTICS = TrainingStatistics(
    id = TrainingStatisticsId.create(),
    trainingPlanId = TrainingPlanId.create(),
    totalTime = 45.minutes,
    date = Date(),
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
            detailsUiState = DetailsUiState.NoData,
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
            detailsUiState = DetailsUiState.NoData,
            onSessionClicked = {},
            onDetailsDismiss = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

