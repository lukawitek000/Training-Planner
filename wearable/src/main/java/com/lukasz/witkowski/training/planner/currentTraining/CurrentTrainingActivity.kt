package com.lukasz.witkowski.training.planner.currentTraining

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.wear.widget.SwipeDismissFrameLayout
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.currentTraining.service.TrainingService
import com.lukasz.witkowski.training.planner.databinding.ActivityCurrentTrainingBinding
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.summary.TrainingSummaryActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.lang.Exception

@AndroidEntryPoint
class CurrentTrainingActivity : FragmentActivity() {

    companion object {
        private const val TRAINING_EXERCISE_TAG = "Training exercise"
        private const val TRAINING_REST_TIME_TAG = "Training rest time"
    }

    private lateinit var trainingService: TrainingService
    private var trainingId = 0L

    private lateinit var binding: ActivityCurrentTrainingBinding
    private val viewModel: CurrentTrainingViewModel by viewModels()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as TrainingService.LocalBinder).getService()
            val training = (viewModel.trainingWithExercises.value as? ResultHandler.Success)?.value
            if (!trainingService.isTrainingStarted() && training != null) {
                trainingService.startTraining(training)
            }
            navigateToState(trainingService.currentTrainingProgressHelper.currentTrainingState.value)
            observeNavigation()
            observeHealthIndicatorsSupport()
            observeHealthService()
        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrentTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchTrainingInformation()
        setOnSwipeListener()
    }

    private fun setOnSwipeListener() {
        findViewById<SwipeDismissFrameLayout>(R.id.swipe_dismiss_layout).apply {
            addCallback(object : SwipeDismissFrameLayout.Callback() {
                override fun onDismissed(layout: SwipeDismissFrameLayout?) {
                    stopCurrentTrainingService()
                    finish()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unbindService(connection)
        } catch (e: Exception) {
            Timber.d("Unbinding service failed ${e.localizedMessage}")
        }

    }

    private fun fetchTrainingInformation() {
        val trainingId = intent.extras?.getLong(StartTrainingActivity.TRAINING_ID_KEY)
        Timber.d("Received training id $trainingId")
        if (trainingId == null) {
            finish()
            return
        }
        this.trainingId = trainingId
        viewModel.trainingWithExercises.observe(this) {
            when(it){
                is ResultHandler.Loading -> showProgressBar()
                is ResultHandler.Success -> startTraining()
                is ResultHandler.Error -> handleError()
            }
            startTrainingService()
        }
        viewModel.fetchTraining(trainingId)
    }

    private fun handleError() {
        Toast.makeText(this, "No training in database", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showProgressBar() {
        binding.swipeDismissLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun startTraining() {
        binding.swipeDismissLayout.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        startTrainingService()
    }

    private fun startTrainingService() {
        val serviceIntent = Intent(this, TrainingService::class.java)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun observeNavigation() {
        trainingService.currentTrainingProgressHelper.currentTrainingState.observe(this) {
            Timber.d("State has changed $it")
            navigateToState(it)
        }
    }

    private fun navigateToState(it: CurrentTrainingState?) {
        val trainingExerciseFragment =
            supportFragmentManager.findFragmentByTag(TRAINING_EXERCISE_TAG)
        val restTimeFragment = supportFragmentManager.findFragmentByTag(TRAINING_REST_TIME_TAG)
        // Navigate to the new state
        when (it) {
            is CurrentTrainingState.ExerciseState -> navigate(
                current = restTimeFragment,
                destination = trainingExerciseFragment,
                destinationTag = TRAINING_EXERCISE_TAG
            )
            is CurrentTrainingState.RestTimeState -> navigate(
                current = trainingExerciseFragment,
                destination = restTimeFragment,
                destinationTag = TRAINING_REST_TIME_TAG
            )
            CurrentTrainingState.SummaryState -> navigateToSummary()
        }
    }

    private fun navigate(current: Fragment?, destination: Fragment?, destinationTag: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        setAnimation(fragmentTransaction)
        if (destination != null) {
            fragmentTransaction.show(destination)
        } else {
            fragmentTransaction.add(
                R.id.current_training_fragment_container,
                getFragmentInstance(destinationTag),
                destinationTag
            )
        }
        if (current != null) {
            fragmentTransaction.hide(current)
        }
        fragmentTransaction.commit()
    }

    private fun setAnimation(fragmentTransaction: FragmentTransaction) {
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
    }

    private fun getFragmentInstance(destinationTag: String): Fragment {
        return if (destinationTag == TRAINING_EXERCISE_TAG) {
            TrainingExerciseFragment()
        } else {
            TrainingRestTimeFragment()
        }
    }

    private fun navigateToSummary() {
        stopCurrentTrainingService()
        val intent = Intent(this, TrainingSummaryActivity::class.java)
        startActivity(intent)
    }

    private fun stopCurrentTrainingService() {
        trainingService.stopCurrentService()
    }

    private fun observeHealthIndicatorsSupport() {
        trainingService.isHeartRateSupported.observe(this) {
            Timber.d("Is Heart rate supported = $it")
            if (!it) {
                Toast.makeText(this, "The Heart rate is not supported", Toast.LENGTH_SHORT).show()
            }
        }
        trainingService.isBurntKcalSupported.observe(this) {
            Timber.d("Is burnt calories supported = $it")
            if (!it) {
                Toast.makeText(this, "The burnt Kcal is not supported", Toast.LENGTH_SHORT).show()
            }
        }
        trainingService.isWorkoutExerciseSupported.observe(this) {
            Timber.d("Is workout supported = $it")
            if (!it) {
                Toast.makeText(this, "The Workout exercise is not supported", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun observeHealthService() {
        trainingService.exerciseUpdatesEndedMessage.observe(this) {
            if (it.isNotEmpty()) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}