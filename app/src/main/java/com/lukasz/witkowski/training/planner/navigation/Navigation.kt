package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lukasz.witkowski.training.planner.ui.*
import com.lukasz.witkowski.training.planner.ui.TrainingsList.TrainingsListViewModel

@Composable
fun Navigation(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(navController = navController, startDestination = BottomNavItem.Trainings.route) {
        composable(BottomNavItem.Trainings.route){
            val trainingsListViewModel: TrainingsListViewModel = viewModel()
            TrainingsScreen(innerPadding = innerPadding, viewModel = trainingsListViewModel)
        }
        composable(BottomNavItem.Exercises.route){
            ExercisesScreen(innerPadding = innerPadding){
                navController.navigate(route = "create-exercise")
            }
        }
        composable(BottomNavItem.Calendar.route){
            CalendarScreen()
        }
        composable(BottomNavItem.Statistics.route){
            StatisticsScreen()
        }
        composable("create-exercise"){
            CreateExerciseScreen()
        }

    }
}

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = BottomNavItem.Items.list,
    backStackEntry: NavBackStackEntry?,
    onItemClick: (BottomNavItem) -> Unit
) {
    BottomNavigation (
        modifier = modifier,
        elevation = 5.dp
    ) {
        items.forEach { item ->
            val selected = item.route == backStackEntry?.destination?.route
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

