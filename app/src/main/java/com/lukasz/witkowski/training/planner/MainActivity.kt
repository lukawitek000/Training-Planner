package com.lukasz.witkowski.training.planner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.navigation.BottomNavigationBar
import com.lukasz.witkowski.training.planner.navigation.NavItem
import com.lukasz.witkowski.training.planner.navigation.Navigation
import com.lukasz.witkowski.training.planner.navigation.TopBar
import com.lukasz.witkowski.training.planner.service.SendingDataService
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startSendingDataService()
        setContent {
            TrainingPlannerTheme {
                TrainingPlannerApp()
            }
        }
    }

    private fun startSendingDataService() {
        val intent = Intent(this, SendingDataService::class.java)
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, SendingDataService::class.java)
        stopService(intent)
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