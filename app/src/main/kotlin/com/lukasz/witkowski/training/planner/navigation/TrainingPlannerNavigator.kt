package com.lukasz.witkowski.training.planner.navigation

import androidx.navigation3.runtime.NavBackStack
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import timber.log.Timber

class TrainingPlannerNavigator(
    val backStack: NavBackStack<TrainingPlannerNavKey>
) {
    fun current() = backStack.last()

    fun goBack() {
        backStack.removeLastOrNull()
    }

    fun exerciseList() {
        backStack.clear()
        backStack.add(ExercisesList)
    }

    fun isExerciseList() = backStack.lastOrNull() == ExercisesList

    fun exerciseDetails(exerciseId: ExerciseId) {
        backStack.add(ExerciseDetails(exerciseId))
    }

    fun exerciseCreate() {
        backStack.add(CreateExercise)
    }

    fun exerciseCreated(exerciseId: ExerciseId) {
        backStack.removeLastOrNull()
        backStack.add(ExerciseDetails(exerciseId))
    }

    fun exerciseEdit(exerciseId: ExerciseId) {
        backStack.add(EditExercise(exerciseId))
    }

    fun showDeleteExerciseDialog(exerciseId: ExerciseId) {
        backStack.add(DeleteExercise(exerciseId))
    }

    fun exerciseDeleted() {
        backStack.clear()
        backStack.add(ExercisesList)
    }

    fun trainingPlansList() {
        backStack.clear()
        backStack.add(TrainingPlansList)
    }

    fun isTrainingPlansList() = backStack.lastOrNull() == TrainingPlansList

    fun trainingCreate() {
        backStack.add(CreateTrainingPlan)
    }

    fun trainingPlanEdit(id: TrainingPlanId) {
        backStack.add(EditTrainingPlan(id))
    }

    fun trainingPlanDetails(id: TrainingPlanId) {
        backStack.add(TrainingPlanDetails(id))
    }

    fun showDeleteTrainingPlanDialog(id: TrainingPlanId) {
        backStack.add(DeleteTrainingPlan(id))
    }

    fun trainingPlanDeleted() {
        backStack.clear()
        backStack.add(TrainingPlansList)
    }

    fun trainingSession(id: TrainingPlanId) {
        backStack.add(TrainingSession(id))
    }

    fun configureTrainingExercise(id: ExerciseId) {
        backStack.add(TrainingExerciseConfiguration(id))
    }

    fun addTrainingExercises() {
        backStack.add(AddTrainingExercise)
    }

    fun returnToCreateTrainingPlan() {
        while (backStack.last() !is CreateTrainingPlan && backStack.last() !is EditTrainingPlan) {
            backStack.removeLastOrNull() ?: run {
                Timber.e("Unknown back stack state $backStack")
                return
            }
        }
    }

    fun isOnMainScreen(): Boolean = isTrainingPlansList() || isExerciseList()

    fun isCreatingOrEditingTrainingPlan() = backStack.contains(CreateTrainingPlan) || backStack.any { it is EditTrainingPlan }
}
