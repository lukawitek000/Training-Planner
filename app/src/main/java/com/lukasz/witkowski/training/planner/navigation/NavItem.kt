package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.lukasz.witkowski.training.planner.R

sealed class NavItem(
    val route: String,
    val icon: Int?,
    val title: String,
    val isBackArrow: Boolean = false
) {

    object Trainings : NavItem("trainings", R.drawable.trainings_icon, "Trainings")
    object Exercises : NavItem("exercises", R.drawable.exercises_icon, "Exercises")
    object Calendar : NavItem("calendar", R.drawable.exercises_icon, "Calendar")
    object Statistics : NavItem("statistics", R.drawable.exercises_icon, "Statistics")

    object BottomNavItems {
        val list = listOf(
            Trainings, Exercises, /* Calendar, Statistics */
        )
    }

    object CreateExercise : NavItem("create-exercise", null, "Create Exercise", true)

    object CreateTrainingGraph :
        NavItem("create-training-graph", null, "Create Training Graph", true)

    object CreateTraining : NavItem("create-training", null, "Create Training", true)

    object PickExercise : NavItem("pick-exercise", null, "Pick Exercise", true)

    object TrainingOverview : NavItem("training-overview", null, "Training overview", true)

    object CurrentTraining: NavItem("current-training", null, "Current training", true)

    object Items {
        val list = listOf<NavItem>(
            Trainings,
            Exercises,
            Calendar,
            Statistics,
            CreateExercise,
            CreateTraining,
            PickExercise,
            CreateTrainingGraph,
            TrainingOverview,
            CurrentTraining
        )
    }
}
