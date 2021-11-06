package com.lukasz.witkowski.training.planner.ui.createTraining

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.exercisesList.ExerciseListItemContent

@Composable
fun CreateTrainingScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateTrainingViewModel,
    navigateBack: () -> Unit,
    onAddExerciseClicked: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val exercises by viewModel.exercises.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.createTraining()
                navigateBack()
            }) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Create training")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TextField(
                text = title,
                onTextChange = { viewModel.onTrainingTitleChanged(it) },
                label = "Title",
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                text = description,
                onTextChange = { viewModel.onTrainingDescriptionChanged(it) },
                label = "Description",
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onAddExerciseClicked() }) {
                Text(text = "Add Exercises")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TrainingExercisesList(exercises = exercises)
        }
    }
}

@Composable
fun TrainingExercisesList(
    modifier: Modifier = Modifier,
    exercises: List<Exercise>
) {
    LazyColumn() {
        items(exercises) { exercise ->
            ExerciseListItemContent(exercise = exercise)
        }
    }
}