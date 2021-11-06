package com.lukasz.witkowski.training.planner.ui.createTraining

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.ui.exercisesList.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.ui.exercisesList.ExercisesScreenContent
import timber.log.Timber

@Composable
fun PickExerciseScreen(
    modifier: Modifier = Modifier,
    viewModel: ExercisesListViewModel,
    createTrainingViewModel: CreateTrainingViewModel,
    navigateBack: () -> Unit
) {
    val pickedExercises by viewModel.pickedExercises.collectAsState()
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Timber.d("Picked Exercises $pickedExercises")
                createTrainingViewModel.addPickedExercises(pickedExercises)
                navigateBack()
            }) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Pick Exercises")
            }
        }
    ) {
        ExercisesScreenContent(
            viewModel = viewModel,
            pickingExerciseMode = true
        )
    }

}