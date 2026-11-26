package com.lukasz.witkowski.training.planner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lukasz.witkowski.training.planner.navigation.BottomNavigationBar
import com.lukasz.witkowski.training.planner.navigation.HandleNavigationIntents
import com.lukasz.witkowski.training.planner.navigation.Navigation
import com.lukasz.witkowski.training.planner.navigation.TopBar
import com.lukasz.witkowski.training.planner.navigation.TopBarAction
import com.lukasz.witkowski.training.planner.navigation.TrainingPlannerNavKey
import com.lukasz.witkowski.training.planner.navigation.TrainingPlannerNavigator
import com.lukasz.witkowski.training.planner.navigation.TrainingPlansList
import com.lukasz.witkowski.training.planner.navigation.rememberTrainingPlannerNavBackStack
import com.lukasz.witkowski.training.planner.navigation.toUiConfig
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.utils.LocalWeightUnit
import com.lukasz.witkowski.training.planner.shared.utils.WeightUnit
import com.lukasz.witkowski.training.planner.user.domain.UserRepository
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val userRepository: UserRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val weightUnit by userRepository.weightUnit.collectAsStateWithLifecycle(
                initialValue = WeightUnit.KG
            )
            CompositionLocalProvider(LocalWeightUnit provides weightUnit) {
                TrainingPlannerTheme {
                    TrainingPlannerApp()
                }
            }
        }
    }
}

@Composable
fun TrainingPlannerApp(initialKey: TrainingPlannerNavKey = TrainingPlansList) {
    val backStack = rememberTrainingPlannerNavBackStack(initialKey)
    val context = LocalContext.current
    val navigator = remember(backStack) { TrainingPlannerNavigator(backStack) }
    HandleNavigationIntents(navigator = navigator)
    val uiConfig = navigator.toUiConfig(context = context)
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                config = uiConfig.bottomBarConfig
            )
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
                            navigator.trainingPlanEdit(it.id)
                        }
                        is TopBarAction.DeleteTrainingPlan -> {
                            navigator.showDeleteTrainingPlanDialog(it.id)
                        }
                        TopBarAction.UserProfile -> {
                            navigator.userProfile()
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            uiConfig.fabConfig?.let { fab ->
                FloatingActionButton(
                    onClick = fab.onClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = fab.icon,
                        contentDescription = fab.contentDescription
                    )
                }
            }
        },
    ) {
        Navigation(
            navigator = navigator,
            modifier = Modifier
                .padding(it)
                .consumeWindowInsets(it)
                .imePadding(),
        )
    }
}

@ExperimentalAnimationApi
@AppPreview
@Composable
fun TrainingPlannerPreview() {
    TrainingPlannerTheme {
        TrainingPlannerApp()
    }
}
