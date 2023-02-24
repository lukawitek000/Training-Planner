package com.lukasz.witkowski.training.planner.training.trainingSession

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.shared.time.formatToString
import com.lukasz.witkowski.training.planner.shared.utils.toPercentage
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import com.lukasz.witkowski.training.planner.training.trainingSession.components.FabTextWithIcon
import com.lukasz.witkowski.training.planner.ui.components.ExpandableListCardItem
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12

@Composable
fun TrainingSessionSummaryScreen(
    modifier: Modifier = Modifier,
    statistics: TrainingStatistics,
    trainingPlan: TrainingPlan
) {
    LazyColumn(modifier.fillMaxSize()) {
        item {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = trainingPlan.title,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondary
            )

            TrainingStatisticsSummary(trainingStatistics = statistics)
        }
        items(statistics.exercisesStatistics) {
            ExerciseStatisticsCard(
                exerciseStatistics = it,
                trainingExercise = trainingPlan.exercises.first { exercise -> exercise.id == it.trainingExerciseId }
            )
        }
    }

}

@Composable
fun TrainingStatisticsSummary(
    modifier: Modifier = Modifier,
    trainingStatistics: TrainingStatistics
) {
    Column(modifier.padding(8.dp)) {
        Text(
            text = stringResource(
                id = R.string.total_time,
                trainingStatistics.totalTime.toString()
            ), fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                id = R.string.effective_time,
                trainingStatistics.effectiveTime.toString()
            ), fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(id = R.string.date, trainingStatistics.date.formatToString()),
            fontSize = 18.sp
        )
    }
}

@Composable
fun ExerciseStatisticsCard(
    modifier: Modifier = Modifier,
    exerciseStatistics: ExerciseStatistics,
    trainingExercise: TrainingExercise
) {
    ExpandableListCardItem(
        modifier = modifier,
        shrinkedContent = {
            ExerciseStatisticsSummary(
                exerciseStatistics = exerciseStatistics,
                trainingExercise = trainingExercise
            )
        },
        expandedContent = { ExerciseDetailStatistics(exerciseStatistics = exerciseStatistics) }
    )
}

@Composable
fun ExerciseStatisticsSummary(
    modifier: Modifier = Modifier,
    exerciseStatistics: ExerciseStatistics,
    trainingExercise: TrainingExercise
) {
    Column(modifier) {
        Text(text = trainingExercise.exercise.name, fontSize = 28.sp, color = MaterialTheme.colors.secondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(id = R.string.total_time, exerciseStatistics.totalTime.toString()))
        Text(
            stringResource(
                id = R.string.completeness_rate,
                exerciseStatistics.completenessRate.toPercentage()
            )
        )
    }
}

@Composable
fun ExerciseDetailStatistics(
    modifier: Modifier = Modifier,
    exerciseStatistics: ExerciseStatistics
) {
    LazyColumn(modifier = modifier.heightIn(max = 500.dp)) {
        items(exerciseStatistics.attemptsStatistics) {
            ListCardItem(backgroundColor = LightDark12) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1.0f)) {
                        Text(text = stringResource(id = R.string.attempt_set, it.set))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(id = R.string.attempt_time, it.time.toString()))
                    }
                    CompletedOrSkippedMark(
                        modifier = Modifier.weight(1.0f),
                        attemptStatistics = it
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedOrSkippedMark(
    modifier: Modifier = Modifier,
    attemptStatistics: ExerciseAttemptStatistics
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (attemptStatistics.completed) {
            FabTextWithIcon(
                backgroundColor = Color.Green,
                text = stringResource(id = R.string.completed),
                imageVector = Icons.Filled.Check,
                onClick = {})
        } else {
            FabTextWithIcon(
                backgroundColor = Color.Red,
                text = stringResource(id = R.string.skipped),
                imageVector = Icons.Filled.SkipNext,
                onClick = {})
        }
    }
}
