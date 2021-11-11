package com.lukasz.witkowski.training.wearable.currentTraining

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.lukasz.witkowski.training.wearable.R
import com.lukasz.witkowski.training.wearable.databinding.ActivityCurrentTrainingBinding
import com.lukasz.witkowski.training.wearable.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.wearable.summary.TrainingSummaryActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CurrentTrainingActivity : FragmentActivity() {

    companion object {
        private const val TRAINING_EXERCISE_TAG = "Training exercise"
        private const val TRAINING_REST_TIME_TAG = "Training rest time"
    }

    private lateinit var binding: ActivityCurrentTrainingBinding

    private val viewModel: CurrentTrainingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrentTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigateToState(CurrentTrainingState.ExerciseState)
        observeNavigation()
        fetchTrainingInformation()
    }

    private fun fetchTrainingInformation() {
        val trainingId = intent.extras?.getLong(StartTrainingActivity.TRAINING_ID_KEY)
        if(trainingId == null) {
            finish()
            return
        }
        viewModel.fetchTraining(trainingId)
    }

    private fun observeNavigation() {
        viewModel.currentTrainingState.observe(this) {
            navigateToState(it)
        }
    }

    private fun navigateToState(it: CurrentTrainingState?) {
        val trainingExerciseFragment =
            supportFragmentManager.findFragmentByTag(TRAINING_EXERCISE_TAG)
        val restTimeFragment = supportFragmentManager.findFragmentByTag(TRAINING_REST_TIME_TAG)
        // Navigate to the new state
        when (it) {
            CurrentTrainingState.ExerciseState -> navigate(
                current = restTimeFragment,
                destination = trainingExerciseFragment,
                destinationTag = TRAINING_EXERCISE_TAG
            )
            CurrentTrainingState.RestTimeState -> navigate(
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
        startActivity(intent)
    }
}
