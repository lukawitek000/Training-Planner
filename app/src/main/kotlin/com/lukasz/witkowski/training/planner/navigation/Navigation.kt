package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.lukasz.witkowski.training.planner.TrainingPlannerViewModelFactory
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.createExercise.EditExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseEditorViewModel
import com.lukasz.witkowski.training.planner.exercise.delete.DeleteExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.delete.DeleteExerciseViewModel
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsScreen
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsViewModel
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
    navigator: TrainingPlannerNavigator,
    modifier: Modifier = Modifier,
) {
    val dialogStrategy = remember {
        DialogSceneStrategy<TrainingPlannerNavKey>()
    }
    NavDisplay(
        modifier = modifier,
        backStack = navigator.backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            rememberLoggingNavDecorator(),
        ),
        sceneStrategies = listOf(dialogStrategy),
        entryProvider = entryProvider {
            val trainingsListViewModel: TrainingsListViewModel = trainingPlannerViewModel()
            entry<TrainingPlansList> {
                TrainingsScreen(
                    viewModel = trainingsListViewModel,
                    navigateToTrainingOverview = { navigator.trainingOverview(it) },
                    navigateToTrainingSession = { navigator.trainingSession(it) }
                )
            }

            entry<ExercisesList> {
                val viewModel: ExercisesListViewModel = trainingPlannerViewModel()
                ExercisesScreen(
                    viewModel = viewModel,
                    onExerciseClicked = { id ->
                        navigator.exerciseDetails(id)
                    }
                )
            }

            entry<CreateExercise> {
                val viewModel: ExerciseEditorViewModel = trainingPlannerViewModel()
                CreateExerciseScreen(
                    viewModel = viewModel,
                    navigateToDetails = { id ->
                        navigator.exerciseCreated(id)
                    }
                )
            }

            entry<EditExercise> { key ->
                val viewModel: ExerciseEditorViewModel = trainingPlannerViewModel(
                    factory = remember(key) { TrainingPlannerViewModelFactory(exerciseId = key.exerciseId) }
                )
                EditExerciseScreen(
                    viewModel = viewModel,
                    navigateToDetails = { _ ->
                        navigator.goBack()
                    }
                )
            }

            entry<ExerciseDetails>{ key ->
                val viewModel: ExerciseDetailsViewModel = trainingPlannerViewModel(
                    factory = remember(key) { TrainingPlannerViewModelFactory(exerciseId = key.exerciseId) },
                )
                ExerciseDetailsScreen(
                    viewModel = viewModel
                )
            }

            entry<DeleteExercise>(
                metadata = DialogSceneStrategy.dialog()
            ) { key ->
                val viewModel: DeleteExerciseViewModel = trainingPlannerViewModel(
                    factory = remember(key) { TrainingPlannerViewModelFactory(exerciseId = key.exerciseId) },
                )
                DeleteExerciseScreen(
                    viewModel = viewModel,
                    onDelete = {
                        navigator.exerciseDeleted()
                    },
                    onCancel = {
                        navigator.goBack()
                    }
                )
            }

//            trainingGraph(
//                navigateUp = { backStack.removeLastOrNull() },
//                navigateToPickExercise = { backStack.add(PickExercise) }
//            )
            entry<TrainingOverview> {
                val viewModel: TrainingOverviewViewModel = trainingPlannerViewModel()
                TrainingOverviewScreen(
                    viewModel = viewModel,
                    navigateBack = { navigator.goBack() }
                )
            }
            entry<TrainingSession> {
                val viewModel: TrainingSessionViewModel = trainingPlannerViewModel()
                TrainingSessionScreen(
                    viewModel = viewModel,
                    navigateBack = { navigator.goBack() }
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
    factory: ViewModelProvider.Factory = remember { TrainingPlannerViewModelFactory() },
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(
        LocalViewModelStoreOwner.current
    ) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
): VM {
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

