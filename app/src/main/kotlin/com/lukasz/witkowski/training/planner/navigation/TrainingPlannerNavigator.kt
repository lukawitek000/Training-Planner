package com.lukasz.witkowski.training.planner.navigation

import androidx.navigation3.runtime.NavBackStack
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId

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
        backStack.add(CreateTraining)
    }

    fun trainingOverview(id: TrainingPlanId) {
        backStack.add(TrainingOverview(id))
    }

    fun trainingSession(id: TrainingPlanId) {
        backStack.add(TrainingSession(id))
    }

    fun isOnMainScreen(): Boolean = isTrainingPlansList() || isExerciseList()
}
