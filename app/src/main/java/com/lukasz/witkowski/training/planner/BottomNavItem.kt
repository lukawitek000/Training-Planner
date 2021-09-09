package com.lukasz.witkowski.training.planner

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, var title: String){
    object Trainings : BottomNavItem("trainings", Icons.Outlined.ShoppingCart, "Trainings")
    object Exercises : BottomNavItem("exercises", Icons.Outlined.Send, "Exercises")
    object Calendar : BottomNavItem("calendar", Icons.Outlined.DateRange, "Calendar")
    object Statistics : BottomNavItem("statistics", Icons.Outlined.Star, "Statistics")

    object Items {
        val list = listOf(
            Trainings, Exercises, Calendar, Statistics
        )
    }
}
