package com.lukasz.witkowski.training.planner.training.trainingsList

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.CategoryFilters
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.NoDataMessage

@Composable
fun TrainingsScreen(
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: TrainingsListViewModel,
    onCreateTrainingFabClicked: () -> Unit = {},
    navigateToTrainingOverview: (String) -> Unit,
    navigateToTrainingSession: (TrainingPlanId) -> Unit
) {
    val trainings by viewModel.trainingPlans.collectAsState(emptyList())
    val selectedCategoriesList by viewModel.selectedCategories.collectAsState()

    Scaffold(
        modifier = Modifier.padding(innerPadding),
        floatingActionButton = {
            CreateTrainingFab(onClicked = onCreateTrainingFabClicked)
        }
    ) {
        Column {
            CategoryFilters(
                categories = viewModel.filterCategories,
                selectedCategories = selectedCategoriesList,
                selectCategory = { viewModel.selectCategory(it) }
            )
            if (trainings.isNotEmpty()) {
                TrainingsList(
                    trainings = trainings,
                    navigateToTrainingOverview =  {
                        //navigateToTrainingOverview // TODO revert this changes
                          viewModel.sendTrainingPlan(it)
                                                  },
                    startTrainingSession = { navigateToTrainingSession(it.id) }
                )
            } else {
                NoDataMessage(
                    text = stringResource(id = R.string.no_trainings_info)
                )
            }
        }
    }
}

@Composable
private fun CreateTrainingFab(
    modifier: Modifier = Modifier,
    onClicked: () -> Unit
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = { onClicked() }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.create_training)
        )
    }
}

@Composable
fun TrainingsList(
    modifier: Modifier = Modifier,
    trainings: List<TrainingPlan>,
    navigateToTrainingOverview: (String) -> Unit,
    startTrainingSession: (TrainingPlan) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(trainings) { trainingWithExercises ->
            ListCardItem(modifier = Modifier,
                onCardClicked = { navigateToTrainingOverview(trainingWithExercises.id.value) }) {
                TrainingListItemContent(
                    trainingPlan = trainingWithExercises,
                    startTrainingSession = startTrainingSession
                )
            }
        }
        item { Spacer(modifier = Modifier.height(74.dp)) }
    }
}

@Composable
fun TrainingListItemContent(
    modifier: Modifier = Modifier,
    trainingPlan: TrainingPlan,
    startTrainingSession: (TrainingPlan) -> Unit
) {
    val categories = trainingPlan.getAllCategories() // TODO how to do it better?
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
                text = trainingPlan.title,
                fontSize = 28.sp
            )
            CategoriesRow(
                modifier = modifier.padding(top = 16.dp),
                categories = categories
            )
        }
        Icon(
            modifier = Modifier
                .size(40.dp)
                .clickable { startTrainingSession(trainingPlan) },
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = stringResource(id = R.string.start_training_session),
            tint = MaterialTheme.colors.primary,
        )
    }
}

@Composable
private fun CategoriesRow(
    modifier: Modifier = Modifier,
    categories: List<Category>
) {
    LazyRow(modifier = modifier) {
        items(categories) { item: Category ->
            CategoryChip(
                modifier = Modifier.padding(end = 8.dp),
                category = item,
                fontSize = 14.sp
            )
        }
    }
}
