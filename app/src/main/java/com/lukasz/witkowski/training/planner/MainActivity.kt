package com.lukasz.witkowski.training.planner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lukasz.witkowski.shared.utils.startSendingDataService
import com.lukasz.witkowski.shared.utils.stopSendingDataService
import com.lukasz.witkowski.training.planner.navigation.BottomNavigationBar
import com.lukasz.witkowski.training.planner.navigation.NavItem
import com.lukasz.witkowski.training.planner.navigation.Navigation
import com.lukasz.witkowski.training.planner.navigation.TopBar
import com.lukasz.witkowski.training.planner.service.SendingTrainingsService
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isServiceStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainingPlannerTheme {
                TrainingPlannerApp()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startSendingTrainingService()
    }

    override fun onStop() {
        super.onStop()
        stopSendingTrainingService()
    }

    override fun onResume() {
        super.onResume()
        startSendingTrainingService()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSendingTrainingService()
    }

    private fun startSendingTrainingService() {
        if (!isServiceStarted) {
            isServiceStarted = startSendingDataService(SendingTrainingsService::class.java)
        }
    }

    private fun stopSendingTrainingService() {
        if (isServiceStarted) {
            isServiceStarted = stopSendingDataService(SendingTrainingsService::class.java)
        }
    }
}

@Composable
fun TrainingPlannerApp() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentScreen = NavItem.Items.list.find {
        it.route == backStackEntry.value?.destination?.route
    } ?: NavItem.Trainings
    // TODO research how to better handle top bar with navigation
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                backStackEntry = backStackEntry.value,
                onItemClick = {
                    navController.navigate(it.route)
                })
        },
        topBar = {
            TopBar(title = currentScreen.title, showBackArrow = currentScreen.isBackArrow) {
                navController.navigateUp()
            }
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