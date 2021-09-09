package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lukasz.witkowski.training.planner.ui.CalendarScreen
import com.lukasz.witkowski.training.planner.ui.ExercisesScreen
import com.lukasz.witkowski.training.planner.ui.StatisticsScreen
import com.lukasz.witkowski.training.planner.ui.TrainingsScreen

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = BottomNavItem.Trainings.route) {
        composable(BottomNavItem.Trainings.route){
            TrainingsScreen()
        }
        composable(BottomNavItem.Exercises.route){
            ExercisesScreen()
        }
        composable(BottomNavItem.Calendar.route){
            CalendarScreen()
        }
        composable(BottomNavItem.Statistics.route){
            StatisticsScreen()
        }
    }
}

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = BottomNavItem.Items.list,
    navController: NavController,
    onItemClick: (BottomNavItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigation (
        modifier = modifier,
        elevation = 5.dp
    ) {
        items.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route
            BottomNavigationItem(
                selected = selected,
                onClick = { onItemClick(item) },
                selectedContentColor = Color.Yellow,
                unselectedContentColor = Color.LightGray,
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = item.icon, contentDescription = item.title)
                        Text(
                            text = item.title,
                            textAlign =  TextAlign.Center
                        )
                    }
                }
            )
        }

    }


}

