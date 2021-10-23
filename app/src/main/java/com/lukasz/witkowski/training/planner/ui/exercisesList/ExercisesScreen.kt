package com.lukasz.witkowski.training.planner.ui.exercisesList

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
        var isExerciseDialogOpen by remember { mutableStateOf(false) }
        var exercise by remember {
            mutableStateOf(exercisesList.firstOrNull())
        }
        if(isExerciseDialogOpen && exercise != null){
            ExerciseInfoAlertDialog(exercise = exercise!!, closeDialog = { isExerciseDialogOpen = false })
        }
        ExercisesList(exercisesList = exercisesList, openDialog = {
            isExerciseDialogOpen = true
            exercise = it
        })
    }
}

@Composable
private fun ExercisesList(
    modifier: Modifier = Modifier,
    exercisesList: List<Exercise>,
    openDialog: (Exercise) -> Unit
) {
    LazyColumn() {
        items(exercisesList) { exercise ->
            ListCardItem(modifier = Modifier.clickable {
                openDialog(exercise)
            }) {
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

@Composable
fun ExerciseInfoAlertDialog(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    closeDialog: () -> Unit
) {
    AlertDialog( modifier = modifier
        .border(width = 1.dp, Color.Yellow)
        .fillMaxWidth(),
        onDismissRequest = closeDialog,
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = exercise.name,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(text = exercise.description)
        },
        buttons = {
            Row(horizontalArrangement = Arrangement.Center) {

            }
            Button(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                onClick = closeDialog
            ) {
                Text(text = "Ok")
            }
        }
    )

}



@Preview(showBackground = true)
@Composable
fun ExercisesScreenPreview() {
    //ExercisesScreen()
}