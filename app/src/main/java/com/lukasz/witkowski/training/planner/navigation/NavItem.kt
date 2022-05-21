package com.lukasz.witkowski.training.planner.navigation

import com.lukasz.witkowski.training.planner.R

sealed class NavItem(
    val route: String,
    val icon: Int?,
    val title: String,
    val isBackArrow: Boolean = false
) {

    object Trainings : NavItem("trainings", R.drawable.trainings_icon, "Trainings")
    object Exercises : NavItem("exercises", R.drawable.exercises_icon, "Exercises")

    object BottomNavItems {
        val list = listOf(
            Trainings, Exercises, /* Calendar, Statistics */
        )
    }

    object CreateExercise : NavItem("create-exercise", null, "Create Exercise", true)
    object EditExercise : NavItem("edit-exercise", null, "Edit Exercise", true)

    object CreateTrainingGraph :
        NavItem("create-training-graph", null, "Create Training Graph", true)
    object CreateTraining : NavItem("create-training", null, "Create Training", true)
    object PickExercise : NavItem("pick-exercise", null, "Pick Exercise", true)

    object TrainingOverview : NavItem("training-overview", null, "Training overview", true)

    object TrainingSession : NavItem("training-session", null, "Training session", true)

    object Items {
        val list = listOf<NavItem>(
            Trainings,
            Exercises,
            CreateExercise,
            EditExercise,
            CreateTraining,
            PickExercise,
            CreateTrainingGraph,
            TrainingOverview,
            TrainingSession
        )
    }
}
