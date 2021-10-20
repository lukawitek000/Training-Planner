package com.lukasz.witkowski.training.planner.ui.exercisesList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.training.planner.ui.ListCardItem

@Composable
fun ExercisesScreen(modifier: Modifier = Modifier,
                    viewModel: ExercisesListViewModel,
                    onCreateExerciseFabClicked: () -> Unit = {}
){
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { onCreateExerciseFabClicked() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Exercise")
            }
        }
    ) {
        val exercisesList by viewModel.exercises.observeAsState(initial = emptyList())
        ExercisesList(exercisesList = exercisesList)
    }
}

@Composable
private fun ExercisesList(
    modifier: Modifier = Modifier,
    exercisesList: List<Exercise>) {
    LazyColumn() {
        items(exercisesList) { exercise ->
            ListCardItem() {
                ExerciseListItemContent(exercise = exercise)
            }
        }
    }
}

@Composable
fun ExerciseListItemContent(
    modifier: Modifier = Modifier,
    exercise: Exercise
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
                .padding(8.dp),
            imageVector = Icons.Filled.Person,
            contentDescription = "${exercise.name} image"
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = exercise.name,
                fontSize = 24.sp
            )
            Text(
                text = exercise.description,
                fontSize = 14.sp
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ExercisesScreenPreview() {
    //ExercisesScreen()
}