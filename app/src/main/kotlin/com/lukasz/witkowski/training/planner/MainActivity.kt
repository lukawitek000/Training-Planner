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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.navigation.BottomBarConfig
import com.lukasz.witkowski.training.planner.navigation.BottomNavigationBar
import com.lukasz.witkowski.training.planner.navigation.Navigation
import com.lukasz.witkowski.training.planner.navigation.TopBar
import com.lukasz.witkowski.training.planner.navigation.TopBarAction
import com.lukasz.witkowski.training.planner.navigation.TrainingPlannerNavigator
import com.lukasz.witkowski.training.planner.navigation.TrainingPlannerNavKey
import com.lukasz.witkowski.training.planner.navigation.TrainingPlansList
import com.lukasz.witkowski.training.planner.navigation.ExercisesList
import com.lukasz.witkowski.training.planner.navigation.rememberTrainingPlannerNavBackStack
import com.lukasz.witkowski.training.planner.navigation.toUiConfig
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
fun TrainingPlannerApp(initialKey: TrainingPlannerNavKey = TrainingPlansList) {
    val backStack = rememberTrainingPlannerNavBackStack(initialKey)
    val context = LocalContext.current
    val navigator = TrainingPlannerNavigator(backStack)
    val uiConfig = navigator.toUiConfig(context = context)
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = uiConfig.bottomBarConfig != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                uiConfig.bottomBarConfig?.let { config ->
                    BottomNavigationBar(
                        items = config.items
                    )
                }
            }
        },
        topBar = {
            TopBar(
                config = uiConfig.topBarConfig,
                navigateBack = { navigator.goBack() },
                onAction = {
                    when (it) {
                        is TopBarAction.EditExercise -> {
                            navigator.exerciseEdit(it.id)
                        }
                        is TopBarAction.DeleteExercise -> {
                            navigator.showDeleteExerciseDialog(it.id)
                        }
                        is TopBarAction.EditTrainingPlan -> {
                            // navigator.trainingPlanEdit(it.id)
                        }
                        is TopBarAction.DeleteTrainingPlan -> {
                            navigator.showDeleteTrainingPlanDialog(it.id)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            uiConfig.fabConfig?.let { fab ->
                FloatingActionButton(onClick = fab.onClick) {
                    Icon(
                        imageVector = fab.icon,
                        contentDescription = fab.contentDescription
                    )
                }
            }
        }
    ) {
        Navigation(
            navigator = navigator,
            modifier = Modifier.padding(it),
        )
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