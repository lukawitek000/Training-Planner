package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise

@Composable
fun TrainingExerciseRepsSetsTimeOverviewRow(
    modifier: Modifier = Modifier,
    exercise: TrainingExercise,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.reps), fontSize = fontSize)
            Text(text = exercise.repetitions.toString(), fontSize = fontSize)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.sets), fontSize = fontSize)
            Text(text = exercise.sets.toString(), fontSize = fontSize)
        }
        if (exercise.time > 0L) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.time), fontSize = fontSize)
                Text(text = TimeFormatter.millisToTime(exercise.time), fontSize = fontSize)
            }
        }
    }
}