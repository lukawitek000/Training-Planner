package com.lukasz.witkowski.training.planner

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
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
import androidx.compose.material.DrawerValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lukasz.witkowski.shared.currentTraining.TrainingService
import com.lukasz.witkowski.shared.utils.startSendingDataService
import com.lukasz.witkowski.shared.utils.stopSendingDataService
import com.lukasz.witkowski.training.planner.navigation.BottomNavigationBar
import com.lukasz.witkowski.training.planner.navigation.NavItem
import com.lukasz.witkowski.training.planner.navigation.Navigation
import com.lukasz.witkowski.training.planner.navigation.TopBar
import com.lukasz.witkowski.training.planner.service.PhoneTrainingService
import com.lukasz.witkowski.training.planner.service.SendingTrainingsService
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.lang.Exception

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isServiceStarted = false
    private lateinit var trainingService: PhoneTrainingService

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainingPlannerTheme {
                TrainingPlannerApp(
                    showToast = { showToast(it) },
                    startTrainingService = { startTrainingService(it) },
                    stopTrainingService = { stopCurrentTrainingService() }
                )
            }
        }
    }

    private fun startTrainingService(trainingId: Long) {
        Timber.d("Start training service $trainingId")
        val serviceIntent = Intent(this, PhoneTrainingService::class.java)
        serviceIntent.putExtra(TrainingService.TRAINING_ID_KEY, trainingId)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun stopCurrentTrainingService() {
        Timber.d("Stop current training service ${::trainingService.isInitialized}")
        if(::trainingService.isInitialized) {
            trainingService.stopCurrentService()
//            unbindService(connection)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as PhoneTrainingService.LocalBinder).getService()
            Timber.d("Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

    private fun showToast(message: String) {
        if(message.isEmpty()) return
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
        try {
            unbindService(connection)
        } catch (e: Exception) {
            Timber.w("Unbinding service failed ${e.localizedMessage}")
        }
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
fun TrainingPlannerApp(
    showToast: (String) -> Unit,
    startTrainingService: (Long) -> Unit,
    stopTrainingService: () -> Unit
) {
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
                    })
            }
        },
        topBar = {
            TopBar(title = currentScreen.title, showBackArrow = currentScreen.isBackArrow) {
                stopTrainingService()
                navController.navigateUp()
            }
        },
        scaffoldState = scaffoldState
    ) {
        Navigation(
            navController = navController,
            innerPadding = it,
            showToast = showToast,
            startTrainingService = startTrainingService,
            stopTrainingService = stopTrainingService
        )
    }
}
