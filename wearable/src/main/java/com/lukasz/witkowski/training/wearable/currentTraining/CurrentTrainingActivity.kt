package com.lukasz.witkowski.training.wearable.currentTraining

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.lukasz.witkowski.training.wearable.R
import com.lukasz.witkowski.training.wearable.currentTraining.service.TrainingService
import com.lukasz.witkowski.training.wearable.databinding.ActivityCurrentTrainingBinding
import com.lukasz.witkowski.training.wearable.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.wearable.summary.TrainingSummaryActivity
import com.lukasz.witkowski.training.wearable.summary.TrainingSummaryActivity.Companion.TRAINING_TIME_KEY
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CurrentTrainingActivity : FragmentActivity() {

    companion object {
        private const val TRAINING_EXERCISE_TAG = "Training exercise"
        private const val TRAINING_REST_TIME_TAG = "Training rest time"
    }

    private lateinit var trainingService: TrainingService
    private var isBound = false
    private var trainingId = 0L

    private lateinit var binding: ActivityCurrentTrainingBinding

    private val viewModel: CurrentTrainingViewModel by viewModels()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as TrainingService.LocalBinder).getService()
            trainingService.startTraining(viewModel.trainingWithExercises!!)
            navigateToState(trainingService.currentTrainingProgressHelper.currentTrainingState.value)
            observeNavigation()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrentTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val serviceIntent = Intent(this, TrainingService::class.java)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        val extras = intent.extras?.getString("NotificationMessage") ?: "No message"
        Timber.d("Extra data received $extras")

//        navigateToState(CurrentTrainingState.ExerciseState)
//        observeNavigation()
        fetchTrainingInformation()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }


    private fun fetchTrainingInformation() {
        val trainingId = intent.extras?.getLong(StartTrainingActivity.TRAINING_ID_KEY)
        Timber.d("Received training id $trainingId")
        if (trainingId == null) {
            finish()
            return
        }
        this.trainingId = trainingId

        viewModel.fetchTraining(trainingId)
    }

    private fun observeNavigation() {
//        viewModel.currentTrainingState.observe(this) {
//            navigateToState(it)
//        }
        trainingService.currentTrainingProgressHelper.currentTrainingState.observe(this) {
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
        val intent = Intent(this, TrainingSummaryActivity::class.java)
        // TODO How to pass training summary?
        // Maybe only values displayed on the summary screen?
        intent.putExtra(TRAINING_TIME_KEY, viewModel.trainingTime)
        startActivity(intent)
    }
}
