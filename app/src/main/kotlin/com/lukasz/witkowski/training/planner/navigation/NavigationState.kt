package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack

class NavigationState(
    val startRoute: TrainingPlannerNavKey,
) {
    val backStack: NavBackStack<TrainingPlannerNavKey> = NavBackStack(startRoute)
}
