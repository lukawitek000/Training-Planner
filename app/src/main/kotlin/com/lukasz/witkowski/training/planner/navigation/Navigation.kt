package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
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
import com.lukasz.witkowski.training.planner.TrainingPlannerApplication
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.createExercise.EditExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.createExercise.EditExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesListViewModelFactory
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
            val trainingsListViewModel: TrainingsListViewModel = hiltViewModel()
            TrainingsScreen(
                innerPadding = innerPadding,
                viewModel = trainingsListViewModel,
                onCreateTrainingFabClicked = { navController.navigate(route = NavItem.CreateTraining.route) },
                navigateToTrainingOverview = { navController.navigate(route = "${NavItem.TrainingOverview.route}/$it") },
                navigateToTrainingSession = { navController.navigate(route = "${NavItem.TrainingSession.route}/$it") }
            )
        }

        composable(NavItem.Exercises.route) {
            // https://programmer.ink/think/a-new-way-to-create-a-viewmodel-creationextras.html
            Timber.d("LWWW compose create exercises list viewmodel")
//            val owner = LocalViewModelStoreOwner.current
//            val defaultExtras = (owner as? HasDefaultViewModelProviderFactory)?.defaultViewModelCreationExtras ?: CreationExtras.Empty
//            val extras = MutableCreationExtras(defaultExtras)
//            extras.apply {
//                set(ViewModelProvider.NewInstanceFactory.VIEW_MODEL_KEY, "1")
//            }
            val factory = remember { ExercisesListViewModelFactory() }
//            Timber.d("LWWW factory $factory")
//            val viewModel: ExercisesListViewModel = remember { factory.create(ExercisesListViewModel::class.java, extras) }
//            Timber.d("LWWW viewmodel $viewModel")
//            val viewModel: ExercisesListViewModel = hiltViewModel()
            val viewModel: ExercisesListViewModel = viewModel(factory = factory)
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
                    nullable = true
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

        composable(NavItem.CreateTraining.route) {
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
