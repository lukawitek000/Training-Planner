package com.lukasz.witkowski.training.planner.ui.trainingsList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.categoriesWithoutNone
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.NoDataMessage
import com.lukasz.witkowski.training.planner.ui.exercisesList.CategoryFilters

@Composable
fun TrainingsScreen(
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: TrainingsListViewModel,
    onCreateTrainingFabClicked: () -> Unit = {},
    navigateToTrainingOverview: (Long) -> Unit,
    navigateToCurrentTraining: (Long) -> Unit
) {
    val trainings by viewModel.trainings.collectAsState(emptyList())
    val selectedCategoriesList by viewModel.selectedCategories.collectAsState()

    Scaffold(
        modifier = Modifier.padding(innerPadding),
        floatingActionButton = {
            FloatingActionButton(onClick = { onCreateTrainingFabClicked() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Training")
            }
        }
    ) {
        Column() {
            CategoryFilters(
                categories = categoriesWithoutNone,
                selectedCategories = selectedCategoriesList,
                selectCategory = { viewModel.selectCategory(it) }
            )
            if (trainings.isNotEmpty()) {
                LazyColumn(
                    contentPadding = innerPadding
                ) {
                    items(trainings) { trainingWithExercises ->
                        ListCardItem(modifier = Modifier,
                            onCardClicked = { navigateToTrainingOverview(trainingWithExercises.training.id) }) {
                            TrainingListItemContent(
                                trainingWithExercises = trainingWithExercises,
                                navigateToCurrentTraining = { navigateToCurrentTraining(trainingWithExercises.training.id) }
                            )
                        }
                    }
                }
            } else {
                NoDataMessage(
                    modifier = Modifier,
                    text = "No trainings. Create your first training"
                )
            }
        }
    }
}

@Composable
fun TrainingListItemContent(
    modifier: Modifier = Modifier,
    trainingWithExercises: TrainingWithExercises,
    navigateToCurrentTraining: () -> Unit
) {
    val categories = trainingWithExercises.exercises.map { it.exercise.category }.filter { it != Category.None }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f)
        ) {
            Text(
                text = trainingWithExercises.training.title,
                fontSize = 28.sp
            )
            if(categories.isNotEmpty()){
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow {
                    items(categories) { item: Category ->
                        CategoryChip(
                            modifier = Modifier.padding(end = 8.dp),
                            text = item.name,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        Icon(
            modifier = Modifier
                .size(40.dp)
                .clickable { navigateToCurrentTraining() },
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Start training",
            tint = MaterialTheme.colors.primary,
        )
    }
}
