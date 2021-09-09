package com.lukasz.witkowski.training.planner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lukasz.witkowski.training.planner.navigation.BottomNavItem
import com.lukasz.witkowski.training.planner.navigation.BottomNavigationBar
import com.lukasz.witkowski.training.planner.navigation.Navigation
import com.lukasz.witkowski.training.planner.navigation.TopBar
import com.lukasz.witkowski.training.planner.ui.CalendarScreen
import com.lukasz.witkowski.training.planner.ui.ExercisesScreen
import com.lukasz.witkowski.training.planner.ui.StatisticsScreen
import com.lukasz.witkowski.training.planner.ui.TrainingsScreen
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainingPlannerTheme {
               TrainingPlannerApp()
            }
        }
    }
}

@Composable
fun TrainingPlannerApp() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentScreenTitle = BottomNavItem.Items.list.find {
        it.route == backStackEntry.value?.destination?.route
    }?.title ?: BottomNavItem.Trainings.title
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                backStackEntry = backStackEntry.value,
                onItemClick = {
                    navController.navigate(it.route)
                })
        },
        topBar = {
            TopBar(title = currentScreenTitle)
        }
    ) {
        Navigation(navController = navController, it)
    }
}


@Preview(showBackground = true)
@Composable
fun TrainingPlannerPreview() {
    TrainingPlannerTheme {
        TrainingPlannerApp()
    }
}