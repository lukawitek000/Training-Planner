package com.lukasz.witkowski.training.planner.exercise.exercisesList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.exercise.models.Exercise
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.DialogContainer


@Composable
fun ExerciseInfoAlertDialog(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    closeDialog: () -> Unit
) {

    DialogContainer(modifier = modifier,
        closeDialog = closeDialog,
        saveData = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = exercise.name,
                fontSize = 32.sp,
                color = MaterialTheme.colors.primary
            )
            Divider(Modifier.padding(8.dp), color = MaterialTheme.colors.primary)
            ImageWithDefaultPlaceholder(
                modifier = Modifier,
                imageDescription = "${exercise.name} image", image = exercise.image,
                heightMax = 350.dp
            )
            if (exercise.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = exercise.description,
                    modifier = Modifier
                )
            }
            val category = exercise.category
            // TODO how to get rid of this check for spacer??
            if (!category.isNone()) {
                Spacer(modifier = Modifier.height(16.dp))
            }
            CategoryChip(
                modifier = Modifier.padding(top = 4.dp),
                category = category
            )
        }
    }
}