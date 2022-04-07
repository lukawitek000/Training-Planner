package com.lukasz.witkowski.training.planner.trainingSession

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.training.createTraining.ExerciseSetsRepsTimeInfo
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise
import com.lukasz.witkowski.training.planner.trainingSession.components.FabTextWithIcon
import com.lukasz.witkowski.training.planner.trainingSession.components.TimerWithCircularProgressBar
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun RestTimeScreen(
    modifier: Modifier = Modifier,
    timeLeft: Long,
    totalTime: Long,
    nextExercise: TrainingExercise,
    skip: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FabTextWithIcon(
                text = stringResource(id = R.string.skip),
                imageVector = Icons.Filled.SkipNext,
                onClick = skip
            )
        }) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            TimerWithCircularProgressBar(totalTime = totalTime, timeLeft = timeLeft)
            NextExerciseOverview(exercise = nextExercise)
        }
    }
}

@Composable
fun NextExerciseOverview(
    modifier: Modifier = Modifier,
    exercise: TrainingExercise
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(text = stringResource(id = R.string.next_exercise), fontSize = 24.sp)
        ListCardItem {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = exercise.name,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExerciseSetsRepsTimeInfo(exercise)
            }
        }
    }
}

@Preview
@Composable
private fun RestTimeScreenPreview() {
    TrainingPlannerTheme {
        RestTimeScreen(
            timeLeft = 9000,
            totalTime = 10000,
            skip = {},
            nextExercise = TrainingExercise(
                id = TrainingExerciseId(""),
                name = "Next exercise name",
                description = "Next exercise description",
                category = Category(0, R.string.category_back),
                repetitions = 15,
                sets = 3,
                time = 30000,
                restTime = 60000
            )
        )
    }
}