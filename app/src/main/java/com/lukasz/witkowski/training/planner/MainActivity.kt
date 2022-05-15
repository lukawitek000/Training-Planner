package com.lukasz.witkowski.training.planner

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lukasz.witkowski.shared.utils.startSendingDataService
import com.lukasz.witkowski.shared.utils.stopSendingDataService
import com.lukasz.witkowski.training.planner.navigation.BottomNavigationBar
import com.lukasz.witkowski.training.planner.navigation.NavItem
import com.lukasz.witkowski.training.planner.navigation.Navigation
import com.lukasz.witkowski.training.planner.navigation.TopBar
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainingPlannerTheme {
                TrainingPlannerApp(showToast = { showToast(it) })
            }
        }
    }

    private fun showToast(message: String) {
        if(message.isEmpty()) return
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun TrainingPlannerApp(showToast: (String) -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = NavItem.Items.list.find {
        it.route.substringBefore('/') == backStackEntry?.destination?.route?.substringBefore('/')
    } ?: NavItem.Trainings
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                NavItem.BottomNavItems.list.contains(currentScreen),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                BottomNavigationBar(
                    backStackEntry = backStackEntry,
                    onItemClick = {
                        handleBottomMenuItemClicked(navController, it)
                    })
            }
        },
        topBar = {
            TopBar(title = currentScreen.title, showBackArrow = currentScreen.isBackArrow) {
                navController.navigateUp()
            }
        },
        scaffoldState = scaffoldState
    ) {
        Navigation(navController = navController, innerPadding = it, showToast = showToast)
    }
}


private fun handleBottomMenuItemClicked(
    navController: NavHostController,
    it: NavItem
) {
    navController.navigate(it.route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}


@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun TrainingPlannerPreview() {
    TrainingPlannerTheme {
        TrainingPlannerApp({})
    }
}