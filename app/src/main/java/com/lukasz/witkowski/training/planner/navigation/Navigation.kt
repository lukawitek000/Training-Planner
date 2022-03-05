package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.lukasz.witkowski.training.planner.exercise.presentation.CreateExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.presentation.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.ui.*
import com.lukasz.witkowski.training.planner.ui.createExercise.CreateExerciseScreen
import com.lukasz.witkowski.training.planner.ui.createTraining.CreateTrainingScreen
import com.lukasz.witkowski.training.planner.ui.createTraining.PickExerciseScreen
import com.lukasz.witkowski.training.planner.ui.exercisesList.ExercisesScreen
import com.lukasz.witkowski.training.planner.ui.trainingOverview.TrainingOverviewScreen
import com.lukasz.witkowski.training.planner.ui.trainingOverview.TrainingOverviewViewModel
import com.lukasz.witkowski.training.planner.ui.trainingsList.TrainingsScreen

@Composable
fun Navigation(navController: NavHostController, innerPadding: PaddingValues, showToast: (String) -> Unit) {
    NavHost(navController = navController, startDestination = NavItem.Trainings.route) {

        composable(NavItem.Trainings.route) {
            val trainingsListViewModel: TrainingsListViewModel = hiltViewModel()
            TrainingsScreen(
                innerPadding = innerPadding,
                viewModel = trainingsListViewModel,
                onCreateTrainingFabClicked = { navController.navigate(route = NavItem.CreateTraining.route) },
                navigateToTrainingOverview = { navController.navigate(route = "${NavItem.TrainingOverview.route}/$it") }
            )
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
                navigateBack = {
                    showToast(it)
                    navController.navigateUp()
                })
        }
        createTrainingNavGraph(innerPadding, navController)

        composable(
            "${NavItem.TrainingOverview.route}/{trainingId}",
            arguments = listOf(navArgument("trainingId") { type = NavType.LongType })
        ) {
            val viewModel: TrainingOverviewViewModel = hiltViewModel()
            TrainingOverviewScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                navigateBack = { navController.navigateUp() }
            )
        }

    }
}

private fun NavGraphBuilder.createTrainingNavGraph(
    innerPadding: PaddingValues,
    navController: NavHostController
) {
    navigation(
        startDestination = NavItem.CreateTraining.route,
        route = NavItem.CreateTrainingGraph.route
    ) {

        composable(NavItem.CreateTraining.route) {
            val createTrainingBackStackEntry = remember { navController.getBackStackEntry(NavItem.CreateTrainingGraph.route) }
            val createTrainingViewModel: CreateTrainingViewModel =
                hiltViewModel(createTrainingBackStackEntry)
            CreateTrainingScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = createTrainingViewModel,
                navigateBack = { navController.navigateUp() },
                onAddExerciseClicked = { navController.navigate(route = NavItem.PickExercise.route) }
            )

        }
        composable(NavItem.PickExercise.route) {
            val viewModel: ExercisesListViewModel = hiltViewModel()
            val createTrainingBackStackEntry = remember { navController.getBackStackEntry(NavItem.CreateTrainingGraph.route) }
            val createTrainingViewModel: CreateTrainingViewModel =
                hiltViewModel(createTrainingBackStackEntry)
            PickExerciseScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                createTrainingViewModel = createTrainingViewModel,
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}
