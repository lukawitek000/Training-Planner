package com.lukasz.witkowski.training.planner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
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

    @ExperimentalAnimationApi
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

@ExperimentalAnimationApi
@Composable
fun TrainingPlannerApp() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentScreen = NavItem.Items.list.find {
        it.route.substringBefore('/') == backStackEntry.value?.destination?.route?.substringBefore('/')
    } ?: NavItem.Trainings
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                NavItem.BottomNavItems.list.contains(currentScreen),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                BottomNavigationBar(
                    backStackEntry = backStackEntry.value,
                    onItemClick = {
                        navController.navigate(it.route)
                    })
            }
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


@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun TrainingPlannerPreview() {
    TrainingPlannerTheme {
        TrainingPlannerApp()
    }
}