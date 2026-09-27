package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.lukasz.witkowski.training.planner.SnackbarState
import com.lukasz.witkowski.training.planner.TrainingPlannerViewModelFactory
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.createExercise.EditExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.createExercise.EditExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseEditorViewModel
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
import kotlinx.serialization.serializer

@Composable
fun Navigation(
    backStack: NavBackStack<TrainingPlannerNavKey>,
    snackbarState: SnackbarState,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            val trainingsListViewModel: TrainingsListViewModel = trainingPlannerViewModel()
            entry<TrainingPlansList> {
                TrainingsScreen(
                    viewModel = trainingsListViewModel,
                    onCreateTrainingFabClicked = { backStack.add(CreateTraining) },
                    navigateToTrainingOverview = { backStack.add(TrainingOverview(it)) },
                    navigateToTrainingSession = { backStack.add(TrainingSession(it)) }
                )
            }

            entry<ExercisesList> {
                val viewModel: ExercisesListViewModel = trainingPlannerViewModel()
                ExercisesScreen(
                    viewModel = viewModel,
                    onExerciseClicked = {
                        backStack.add(ExerciseDetails(it))
                    }
                )
            }

            entry<CreateExercise> {
                val viewModel: ExerciseEditorViewModel = trainingPlannerViewModel()
                CreateExerciseScreen(
                    viewModel = viewModel,
                )
            }
            entry<EditExercise> {
                val viewModel: EditExerciseViewModel = trainingPlannerViewModel()
                EditExerciseScreen(
                    viewModel = viewModel,
                    snackbarState = snackbarState,
                    navigateUp = { backStack.removeLastOrNull() }
                )
            }
            trainingGraph(
                navigateUp = { backStack.removeLastOrNull() },
                navigateToPickExercise = { backStack.add(PickExercise) }
            )
            entry<TrainingOverview> {
                val viewModel: TrainingOverviewViewModel = trainingPlannerViewModel()
                TrainingOverviewScreen(
                    viewModel = viewModel,
                    navigateBack = { backStack.removeLastOrNull() }
                )
            }
            entry<TrainingSession> {
                val viewModel: TrainingSessionViewModel = trainingPlannerViewModel()
                TrainingSessionScreen(
                    viewModel = viewModel,
                    navigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

private fun EntryProviderScope<TrainingPlannerNavKey>.trainingGraph(
    navigateUp: () -> Unit,
    navigateToPickExercise: () -> Unit
) {
    // TODO avoid destroying VMs
    entry<CreateTraining> {
        val createTrainingViewModel: CreateTrainingViewModel = trainingPlannerViewModel()
        CreateTrainingScreen(
            modifier = Modifier,
            viewModel = createTrainingViewModel,
            navigateBack = { navigateUp() },
            onAddExerciseClicked = { navigateToPickExercise() }
        )

    }

    entry<PickExercise> {
        val viewModel: ExercisesListViewModel = trainingPlannerViewModel()
        val createTrainingViewModel: CreateTrainingViewModel = trainingPlannerViewModel()
        PickExerciseScreen(
            modifier = Modifier,
            viewModel = viewModel,
            createTrainingViewModel = createTrainingViewModel,
            navigateBack = { navigateUp() }
        )
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

@Composable
fun rememberTrainingPlannerNavBackStack(
    vararg elements: TrainingPlannerNavKey,
): NavBackStack<TrainingPlannerNavKey> {
    return rememberSerializable(serializer = serializer()) {
        NavBackStack(*elements)
    }
}

