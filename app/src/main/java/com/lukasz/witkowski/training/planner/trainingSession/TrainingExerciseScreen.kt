package com.lukasz.witkowski.training.planner.trainingSession

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise

@Composable
fun TrainingExerciseScreen(
    modifier: Modifier = Modifier,
    exercise: TrainingExercise
) {
    Text(text = exercise.toString())
}