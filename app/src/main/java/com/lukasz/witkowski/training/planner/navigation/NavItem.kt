package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val route: String, val icon: ImageVector?, var title: String){
    object Trainings : NavItem("trainings", Icons.Outlined.ShoppingCart, "Trainings")
    object Exercises : NavItem("exercises", Icons.Outlined.Send, "Exercises")
    object Calendar : NavItem("calendar", Icons.Outlined.DateRange, "Calendar")
    object Statistics : NavItem("statistics", Icons.Outlined.Star, "Statistics")

    object BottomNavItems {
        val list = listOf(
            Trainings, Exercises, Calendar, Statistics
        )
    }

    object CreateExercise : NavItem("create-exercise", null, "Create Exercise")

    object Items {
        val list = listOf<NavItem>(
            Trainings, Exercises, Calendar, Statistics, CreateExercise
        )
    }
}
