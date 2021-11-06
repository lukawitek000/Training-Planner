package com.lukasz.witkowski.training.planner.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lukasz.witkowski.training.planner.ui.*
import com.lukasz.witkowski.training.planner.ui.createExercise.CreateExerciseScreen
import com.lukasz.witkowski.training.planner.ui.createExercise.CreateExerciseViewModel
import com.lukasz.witkowski.training.planner.ui.createTraining.CreateTrainingScreen
import com.lukasz.witkowski.training.planner.ui.createTraining.CreateTrainingViewModel
import com.lukasz.witkowski.training.planner.ui.createTraining.PickExerciseScreen
import com.lukasz.witkowski.training.planner.ui.exercisesList.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.ui.exercisesList.ExercisesScreen
import com.lukasz.witkowski.training.planner.ui.trainingsList.TrainingsListViewModel
import com.lukasz.witkowski.training.planner.ui.trainingsList.TrainingsScreen

@Composable
fun Navigation(navController: NavHostController, innerPadding: PaddingValues) {

    //val createTrainingViewModel: CreateTrainingViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = NavItem.Trainings.route) {

        composable(NavItem.Trainings.route) {
            val trainingsListViewModel: TrainingsListViewModel = viewModel()
            TrainingsScreen(innerPadding = innerPadding, viewModel = trainingsListViewModel) {
                navController.navigate(route = NavItem.CreateTraining.route)
            }
        }

        composable(NavItem.Exercises.route) {
            val viewModel: ExercisesListViewModel = hiltViewModel()
            ExercisesScreen(Modifier.padding(innerPadding), viewModel = viewModel) {
                navController.navigate(route = NavItem.CreateExercise.route)
            }
        }

        composable(NavItem.Calendar.route) {
            CalendarScreen()
        }

        composable(NavItem.Statistics.route) {
            StatisticsScreen()
        }

        composable(NavItem.CreateExercise.route) {
            val viewModel: CreateExerciseViewModel = hiltViewModel()
            CreateExerciseScreen(
                Modifier.padding(innerPadding),
                viewModel = viewModel,
                navigateBack = { navController.navigateUp() })
        }
        createTrainingNavGraph(innerPadding, navController)



    }
}

@SuppressLint("UnrememberedGetBackStackEntry")
private fun NavGraphBuilder.createTrainingNavGraph(
    innerPadding: PaddingValues,
    navController: NavHostController
) {
    navigation(
        startDestination = NavItem.CreateTraining.route,
        route = NavItem.CreateTrainingGraph.route
    ) {
        composable(NavItem.CreateTraining.route) {
            val createTrainingViewModel: CreateTrainingViewModel =
                hiltViewModel(navController.getBackStackEntry(NavItem.Trainings.route))
            CreateTrainingScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = createTrainingViewModel,
                navigateBack = { navController.navigateUp() },
                onAddExerciseClicked = { navController.navigate(route = NavItem.PickExercise.route) }
            )

        }
        composable(NavItem.PickExercise.route) {
            val viewModel: ExercisesListViewModel = hiltViewModel()
            val createTrainingViewModel: CreateTrainingViewModel =
                hiltViewModel(navController.getBackStackEntry(NavItem.Trainings.route))
            PickExerciseScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                createTrainingViewModel = createTrainingViewModel,
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    items: List<NavItem> = NavItem.BottomNavItems.list,
    backStackEntry: NavBackStackEntry?,
    onItemClick: (NavItem) -> Unit
) {
    BottomNavigation(
        modifier = modifier,
        elevation = 5.dp
    ) {
        items.forEach { item ->
            if (item.icon == null) return@forEach
            val selected = item.route == backStackEntry?.destination?.route
            BottomNavigationItem(
                selected = selected,
                onClick = { onItemClick(item) },
                selectedContentColor = Color.Yellow,
                unselectedContentColor = Color.LightGray,
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = item.icon, contentDescription = item.title)
                        Text(
                            text = item.title,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        }

    }


}

