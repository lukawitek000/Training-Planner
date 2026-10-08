package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.rememberResultEventBusNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.createExercise.EditExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.delete.DeleteExerciseScreen
import com.lukasz.witkowski.training.planner.exercise.details.ExerciseDetailsScreen
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesScreen
import com.lukasz.witkowski.training.planner.training.delete.DeleteTrainingPlanScreen
import com.lukasz.witkowski.training.planner.training.details.TrainingPlanDetailsScreen
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.editor.AddTrainingExerciseScreen
import com.lukasz.witkowski.training.planner.training.editor.EditTrainingPlanScreen
import com.lukasz.witkowski.training.planner.training.editor.TrainingExerciseConfigurationScreen
import com.lukasz.witkowski.training.planner.training.editor.TrainingPlanEditingIntent
import com.lukasz.witkowski.training.planner.training.editor.TrainingPlanEditorScreen
import com.lukasz.witkowski.training.planner.training.editor.TrainingPlanEditorViewModel
import com.lukasz.witkowski.training.planner.training.list.TrainingsScreen
import com.lukasz.witkowski.training.planner.training.trainingSession.TrainingSessionScreen
import kotlinx.serialization.serializer
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

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
            rememberResultEventBusNavEntryDecorator()
        ),
        sceneStrategies = listOf(dialogStrategy),
        entryProvider = entryProvider {
            entry<TrainingPlansList> {
                TrainingsScreen(
                    viewModel = koinViewModel(),
                    onTrainingPlanClicked = { navigator.trainingPlanDetails(it) },
                )
            }

            entry<CreateTrainingPlan> {
                val trainingPlanEditorViewModel: TrainingPlanEditorViewModel = koinViewModel()
                ResultEffect<TrainingExercise> { trainingExercise ->
                    Timber.i("TrainingExercise received $trainingExercise")
                    trainingPlanEditorViewModel.processIntent(
                        TrainingPlanEditingIntent.TrainingExerciseAdded(trainingExercise)
                    )
                }
                TrainingPlanEditorScreen(
                    viewModel = trainingPlanEditorViewModel,
                    onAddExerciseClicked = { navigator.addTrainingExercises() },
                    navigateBack = { navigator.goBack() }
                )

            }

            entry<EditTrainingPlan> { key ->
                val trainingPlanEditorViewModel: TrainingPlanEditorViewModel = koinViewModel { parametersOf(key.trainingPlanId) }
                ResultEffect<TrainingExercise> { trainingExercise ->
                    Timber.i("TrainingExercise received $trainingExercise")
                    trainingPlanEditorViewModel.processIntent(
                        TrainingPlanEditingIntent.TrainingExerciseAdded(trainingExercise)
                    )
                }
                EditTrainingPlanScreen(
                    viewModel = trainingPlanEditorViewModel,
                    onAddExerciseClicked = { navigator.addTrainingExercises() },
                    navigateBack = { navigator.goBack() }
                )
            }

            entry<AddTrainingExercise> {
                AddTrainingExerciseScreen(
                    viewModel = koinViewModel(),
                    onExerciseSelected = { navigator.configureTrainingExercise(it) }
                )
            }

            entry<TrainingExerciseConfiguration> { key ->
                val resultBus = LocalResultEventBus.current
                TrainingExerciseConfigurationScreen(
                    viewModel = koinViewModel { parametersOf(key.exerciseId) },
                    onExerciseConfigured = { trainingExercise ->
                        Timber.i("TrainingExercise configured $trainingExercise")
                        resultBus.sendResult(trainingExercise)
                        navigator.returnToCreateTrainingPlan()
                    }
                )
            }

            entry<TrainingPlanDetails> { key ->
                TrainingPlanDetailsScreen(
                    viewModel = koinViewModel { parametersOf(key.trainingPlanId) },
                )
            }

            entry<DeleteTrainingPlan>(
                metadata = DialogSceneStrategy.dialog()
            ) { key ->
                DeleteTrainingPlanScreen(
                    viewModel = koinViewModel { parametersOf(key.trainingPlanId) },
                    onDelete = {
                        navigator.trainingPlanDeleted()
                    },
                    onCancel = {
                        navigator.goBack()
                    }
                )
            }

            exerciseEntryBuilder(navigator)

//            trainingGraph(
//                navigateUp = { backStack.removeLastOrNull() },
//                navigateToPickExercise = { backStack.add(PickExercise) }
//            )

            entry<TrainingSession> {
                TrainingSessionScreen(
                    viewModel = koinViewModel(),
                    navigateBack = { navigator.goBack() }
                )
            }
        }
    )
}

private fun EntryProviderScope<TrainingPlannerNavKey>.exerciseEntryBuilder(
    navigator: TrainingPlannerNavigator
) {
    entry<ExercisesList> {
        ExercisesScreen(
            viewModel = koinViewModel(),
            onExerciseClicked = { id ->
                navigator.exerciseDetails(id)
            }
        )
    }

    entry<CreateExercise> {
        CreateExerciseScreen(
            viewModel = koinViewModel(),
            navigateToDetails = { id ->
                navigator.exerciseCreated(id)
            }
        )
    }

    entry<EditExercise> { key ->
        EditExerciseScreen(
            viewModel = koinViewModel { parametersOf(key.exerciseId) },
            navigateToDetails = { _ ->
                navigator.goBack()
            }
        )
    }

    entry<ExerciseDetails> { key ->
        ExerciseDetailsScreen(
            viewModel = koinViewModel { parametersOf(key.exerciseId) }
        )
    }

    entry<DeleteExercise>(
        metadata = DialogSceneStrategy.dialog()
    ) { key ->
        DeleteExerciseScreen(
            viewModel = koinViewModel { parametersOf(key.exerciseId) },
            onDelete = {
                navigator.exerciseDeleted()
            },
            onCancel = {
                navigator.goBack()
            }
        )
    }
}

@Composable
fun rememberTrainingPlannerNavBackStack(
    vararg elements: TrainingPlannerNavKey,
): NavBackStack<TrainingPlannerNavKey> {
    return rememberSerializable(serializer = serializer()) {
        NavBackStack(*elements)
    }
}

