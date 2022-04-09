package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import com.lukasz.witkowski.shared.time.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise

@Composable
fun TrainingExerciseRepsSetsTimeOverviewRow(
    modifier: Modifier = Modifier,
    exercise: TrainingExercise,
    fontSize: TextUnit = TextUnit.Unspecified,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.reps), fontSize = fontSize, color = textColor)
            Text(text = exercise.repetitions.toString(), fontSize = fontSize, color = textColor)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.sets), fontSize = fontSize, color = textColor)
            Text(text = exercise.sets.toString(), fontSize = fontSize, color = textColor)
        }
        if (exercise.time > 0L) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.time), fontSize = fontSize, color = textColor)
                Text(text = TimeFormatter.millisToTime(exercise.time), fontSize = fontSize, color = textColor)
            }
        }
    }
}