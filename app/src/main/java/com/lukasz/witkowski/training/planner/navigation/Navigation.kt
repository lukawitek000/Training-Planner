package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.lukasz.witkowski.training.planner.SnackbarState
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.createExercise.EditExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.createExercise.EditExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesScreen
import com.lukasz.witkowski.training.planner.training.createTraining.CreateTrainingScreen
import com.lukasz.witkowski.training.planner.training.createTraining.CreateTrainingViewModel
import com.lukasz.witkowski.training.planner.training.createTraining.PickExerciseScreen
import com.lukasz.witkowski.training.planner.training.trainingOverview.TrainingOverviewScreen
import com.lukasz.witkowski.training.planner.training.trainingOverview.TrainingOverviewViewModel
import com.lukasz.witkowski.training.planner.training.trainingSession.TrainingSessionScreen
import com.lukasz.witkowski.training.planner.training.trainingSession.TrainingSessionViewModel
import com.lukasz.witkowski.training.planner.training.trainingsList.TrainingsListViewModel
import com.lukasz.witkowski.training.planner.training.trainingsList.TrainingsScreen

@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    snackbarState: SnackbarState
) {
    NavHost(navController = navController, startDestination = NavItem.Trainings.route) {

        composable(NavItem.Trainings.route) {
            val trainingsListViewModel: TrainingsListViewModel = hiltViewModel()
            TrainingsScreen(
                innerPadding = innerPadding,
                viewModel = trainingsListViewModel,
                onCreateTrainingFabClicked = { navController.navigate(route = NavItem.CreateTraining.route) },
                navigateToTrainingOverview = { navController.navigate(route = "${NavItem.TrainingOverview.route}/$it") },
                navigateToTrainingSession = { navController.navigate(route = "${NavItem.TrainingSession.route}/${it.value}") },
                navigateToTrainingPlanEditScreen = { navController.navigate(route = "${NavItem.CreateTraining.route}?trainingPlanId=${it.value}") }
            )
        }

        composable(NavItem.Exercises.route) {
            val viewModel: ExercisesListViewModel = hiltViewModel()
            ExercisesScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                snackbarState = snackbarState,
                navigateToExerciseCreateScreen = {
                    navController.navigate(NavItem.CreateExercise.route)
                },
                navigateToExerciseEditScreen = {
                    navController.navigate("${NavItem.EditExercise.route}/${it.value}")
                })
        }

        composable(NavItem.CreateExercise.route) {
            val viewModel: CreateExerciseViewModel = hiltViewModel()
            CreateExerciseScreen(
                Modifier.padding(innerPadding),
                viewModel = viewModel,
                snackbarState = snackbarState,
                navigateUp = { navController.navigateUp() }
            )
        }

        composable(
            "${NavItem.EditExercise.route}/{exerciseId}",
            arguments = listOf(
                navArgument("exerciseId") {
                    type = NavType.StringType
                })
        ) {
            val viewModel: EditExerciseViewModel = hiltViewModel()
            EditExerciseScreen(
                Modifier.padding(innerPadding),
                viewModel = viewModel,
                snackbarState = snackbarState,
                navigateUp = { navController.navigateUp() }
            )
        }
        createTrainingNavGraph(innerPadding, navController)

        composable(
            "${NavItem.TrainingOverview.route}/{trainingId}",
            arguments = listOf(navArgument("trainingId") { type = NavType.StringType })
        ) {
            val viewModel: TrainingOverviewViewModel = hiltViewModel()
            TrainingOverviewScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                navigateBack = { navController.navigateUp() }
            )
        }

        composable(
            "${NavItem.TrainingSession.route}/{trainingId}",
            arguments = listOf(navArgument("trainingId") { type = NavType.StringType })
        ) {
            val viewModel: TrainingSessionViewModel = hiltViewModel()
            TrainingSessionScreen(
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
    val padding = PaddingValues.Absolute(
        top = innerPadding.calculateTopPadding(),
        left = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
        right = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
    )
    navigation(
        startDestination = NavItem.CreateTraining.route,
        route = NavItem.CreateTrainingGraph.route
    ) {

        composable(
            route = "${NavItem.CreateTraining.route}?trainingPlanId={trainingPlanId}",
            arguments = listOf(navArgument("trainingPlanId") { nullable = true })
        ) {
            val createTrainingBackStackEntry =
                remember { navController.getBackStackEntry(NavItem.CreateTrainingGraph.route) }
            val createTrainingViewModel: CreateTrainingViewModel =
                hiltViewModel(createTrainingBackStackEntry)
            CreateTrainingScreen(
                modifier = Modifier.padding(padding),
                viewModel = createTrainingViewModel,
                navigateBack = { navController.navigateUp() },
                onAddExerciseClicked = { navController.navigate(route = NavItem.PickExercise.route) }
            )

        }
        composable(NavItem.PickExercise.route) {
            val viewModel: ExercisesListViewModel = hiltViewModel()
            val createTrainingBackStackEntry =
                remember { navController.getBackStackEntry(NavItem.CreateTrainingGraph.route) }
            val createTrainingViewModel: CreateTrainingViewModel =
                hiltViewModel(createTrainingBackStackEntry)
            PickExerciseScreen(
                modifier = Modifier.padding(padding),
                viewModel = viewModel,
                createTrainingViewModel = createTrainingViewModel,
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}
