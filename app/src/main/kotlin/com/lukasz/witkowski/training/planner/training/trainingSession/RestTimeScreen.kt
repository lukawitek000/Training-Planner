package com.lukasz.witkowski.training.planner.training.trainingSession

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.training.trainingSession.components.TimerWithCircularProgressBar
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.TrainingExerciseRepsSetsTimeOverviewRow
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun RestTimeScreen(
    modifier: Modifier = Modifier,
    timeLeft: Time,
    totalTime: Time,
    nextExercise: TrainingExercise
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        TimerWithCircularProgressBar(totalTime = totalTime, timeLeft = timeLeft)
        NextExerciseOverview(trainingExercise = nextExercise)
    }
}

@Composable
fun NextExerciseOverview(
    modifier: Modifier = Modifier,
    trainingExercise: TrainingExercise
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(text = stringResource(id = R.string.next_exercise), fontSize = 24.sp)
        ListCardItem {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = trainingExercise.exercise.name,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                TrainingExerciseRepsSetsTimeOverviewRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    exercise = trainingExercise,
                    textColor = MaterialTheme.colors.primaryVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun RestTimeScreenPreview() {
    TrainingPlannerTheme {
        RestTimeScreen(
            timeLeft = Time(9000),
            totalTime = Time(10000),
            nextExercise = TrainingExercise(
                id = TrainingExerciseId(""),
                exercise = Exercise(
                    ExerciseId.create(),
                    name = "Next exercise name",
                    description = "Next exercise description",
                    category = Category(0, R.string.category_back),
                    null
                ),
                repetitions = 15,
                sets = 3,
                time = Time(30000),
                restTime = Time(60000)
            )
        )
    }
}