package com.lukasz.witkowski.training.planner.ui.createTraining

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.TextField

@Composable
fun CreateTrainingScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateTrainingViewModel,
    navigateBack: () -> Unit,
    onAddExerciseClicked: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val exercises by viewModel.trainingExercises.collectAsState()

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
            TrainingExercisesList(
                exercises = exercises,
                removeTrainingExercise = { viewModel.removeTrainingExercise(it) })
        }
    }
}

@Composable
fun TrainingExercisesList(
    modifier: Modifier = Modifier,
    exercises: List<TrainingExercise>,
    removeTrainingExercise: (TrainingExercise) -> Unit
) {
    LazyColumn() {
        itemsIndexed(exercises) { index, exercise ->
            TrainingExerciseListItem(
                trainingExercise = exercise,
                index = index,
                removeTrainingExercise = removeTrainingExercise
            )
        }
    }
}

@Composable
fun TrainingExerciseListItem(
    modifier: Modifier = Modifier,
    trainingExercise: TrainingExercise,
    index: Int = 0,
    removeTrainingExercise: (TrainingExercise) -> Unit
) {

    ListCardItem() {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${index + 1}.", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(text = trainingExercise.exercise.name, fontSize = 24.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Reps: ${trainingExercise.repetitions}",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Sets: ${trainingExercise.sets}",
                        modifier = Modifier.weight(1f)
                    )
                    if (trainingExercise.time > 0L) {
                        Text(text = "Time: ${TimeFormatter.millisToMinutesSeconds(trainingExercise.time)}")
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        removeTrainingExercise(trainingExercise)
                    },
                imageVector = Icons.Default.Close,
                contentDescription = "Remove training exercise"
            )
        }

    }
}

@Preview
@Composable
fun TrainingExerciseListItemPreview() {
    TrainingExerciseListItem(
        trainingExercise = TrainingExercise(
            exercise = Exercise(name = "New exercise"),
            repetitions = 10,
            sets = 5,
            time = 1000
        ),
        removeTrainingExercise = {}
    )
}