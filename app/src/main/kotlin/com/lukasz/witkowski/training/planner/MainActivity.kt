package com.lukasz.witkowski.training.planner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavBackStack
import com.lukasz.witkowski.training.planner.navigation.BottomBarItem
import com.lukasz.witkowski.training.planner.navigation.BottomNavItems
import com.lukasz.witkowski.training.planner.navigation.BottomNavigationBar
import com.lukasz.witkowski.training.planner.navigation.CreateExercise
import com.lukasz.witkowski.training.planner.navigation.ExercisesList
import com.lukasz.witkowski.training.planner.navigation.Navigation
import com.lukasz.witkowski.training.planner.navigation.TopBar
import com.lukasz.witkowski.training.planner.navigation.TrainingPlannerNavKey
import com.lukasz.witkowski.training.planner.navigation.TrainingPlansList
import com.lukasz.witkowski.training.planner.navigation.rememberTrainingPlannerNavBackStack
import com.lukasz.witkowski.training.planner.ui.components.CustomSnackbar
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrainingPlannerTheme {
                TrainingPlannerApp()
            }
        }
    }
}

@Composable
fun TrainingPlannerApp() {
    val backStack = rememberTrainingPlannerNavBackStack(TrainingPlansList)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarState = remember(scope) {
        SnackbarState(
            scope = scope,
            show = { message, actionLabel ->
                snackbarHostState.showSnackbar(message, actionLabel = actionLabel)
            }
        )
    }
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = backStack.last() in BottomNavItems,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                BottomNavigationBar(
                    items = listOf(
                        BottomBarItem(
                            icon = R.drawable.trainings_icon,
                            title = "Training Plans",
                            selected = backStack.lastOrNull() is TrainingPlansList,
                            onClick = { navigateBottomBar(backStack, TrainingPlansList)}
                        ),
                        BottomBarItem(
                            icon = R.drawable.exercises_icon,
                            title = "Exercises",
                            selected = backStack.lastOrNull() is ExercisesList,
                            onClick = { navigateBottomBar(backStack, ExercisesList)}
                        ),
                    ),
                )
            }
        },
        topBar = {
            TopBar(title = backStack.lastOrNull().toString(), showBackArrow = backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                CustomSnackbar(snackbarData = data)
            }
        },
        floatingActionButton = {
            if (backStack.lastOrNull() == ExercisesList) {
                FloatingActionButton(onClick = { backStack.add(CreateExercise) }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Exercise")
                }
            }
        }
    ) {
        Navigation(
            backStack = backStack,
            modifier = Modifier.padding(it),
            snackbarState = snackbarState
        )
    }
}

private fun navigateBottomBar(
    backStack: NavBackStack<TrainingPlannerNavKey>,
    newDestination: TrainingPlannerNavKey
) {
    if (backStack.last() != newDestination) {
        backStack.removeLastOrNull()
        backStack.add(newDestination)
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