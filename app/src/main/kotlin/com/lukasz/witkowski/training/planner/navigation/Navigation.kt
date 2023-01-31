package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.lukasz.witkowski.training.planner.SnackbarState
import com.lukasz.witkowski.training.planner.TrainingPlannerViewModelFactory
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
import timber.log.Timber

@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    snackbarState: SnackbarState
) {
    NavHost(navController = navController, startDestination = NavItem.Trainings.route) {

        composable(NavItem.Trainings.route) {
            val trainingsListViewModel: TrainingsListViewModel = trainingPlannerViewModel()
            TrainingsScreen(
                innerPadding = innerPadding,
                viewModel = trainingsListViewModel,
                onCreateTrainingFabClicked = { navController.navigate(route = NavItem.CreateTraining.route) },
                navigateToTrainingOverview = { navController.navigate(route = "${NavItem.TrainingOverview.route}/$it") },
                navigateToTrainingSession = { navController.navigate(route = "${NavItem.TrainingSession.route}/$it") }
            )
        }

        createTrainingNavGraph(innerPadding, navController)

        composable(NavItem.Exercises.route) {
            val viewModel: ExercisesListViewModel = trainingPlannerViewModel()
            ExercisesScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                snackbarState = snackbarState,
                navigateToExerciseCreateScreen = {
                    navController.navigate(NavItem.CreateExercise.route)
                },
                navigateToExerciseEditScreen = {
                    navController.navigate("${NavItem.EditExercise.route}/$it")
                })
        }

        composable(NavItem.CreateExercise.route) {
            val viewModel: CreateExerciseViewModel = trainingPlannerViewModel()
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
                    nullable = true
                    type = NavType.StringType
                })
        ) {
            val viewModel: EditExerciseViewModel = trainingPlannerViewModel()
            EditExerciseScreen(
                Modifier.padding(innerPadding),
                viewModel = viewModel,
                snackbarState = snackbarState,
                navigateUp = { navController.navigateUp() }
            )
        }

        composable(
            "${NavItem.TrainingOverview.route}/{trainingId}",
            arguments = listOf(navArgument("trainingId") { type = NavType.StringType })
        ) {
            val viewModel: TrainingOverviewViewModel = trainingPlannerViewModel()
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
            val viewModel: TrainingSessionViewModel = trainingPlannerViewModel()
            TrainingSessionScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                navigateBack = { navController.navigateUp() }
            )
        }

    }
}

@Composable
private inline fun <reified VM : ViewModel> trainingPlannerViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(
        LocalViewModelStoreOwner.current
    ) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
): VM {
    val factory = remember { TrainingPlannerViewModelFactory() }
    return viewModel(viewModelStoreOwner, factory = factory)
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

        composable(NavItem.CreateTraining.route) {
            val createTrainingBackStackEntry = remember(it) { navController.getBackStackEntry(NavItem.CreateTrainingGraph.route) }
            val createTrainingViewModel: CreateTrainingViewModel = trainingPlannerViewModel(createTrainingBackStackEntry)
            CreateTrainingScreen(
                modifier = Modifier.padding(padding),
                viewModel = createTrainingViewModel,
                navigateBack = { navController.navigateUp() },
                onAddExerciseClicked = { navController.navigate(route = NavItem.PickExercise.route) }
            )

        }
        composable(NavItem.PickExercise.route) {
            val viewModel: ExercisesListViewModel = trainingPlannerViewModel()
            val createTrainingBackStackEntry = remember(it) { navController.getBackStackEntry(NavItem.CreateTrainingGraph.route) }
            val createTrainingViewModel: CreateTrainingViewModel = trainingPlannerViewModel(createTrainingBackStackEntry)
            PickExerciseScreen(
                modifier = Modifier.padding(padding),
                viewModel = viewModel,
                createTrainingViewModel = createTrainingViewModel,
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}
