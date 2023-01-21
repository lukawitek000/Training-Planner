package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.wear.widget.SwipeDismissFrameLayout
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingSessionBinding
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingViewModel
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TrainingSessionActivity : FragmentActivity() {

    private lateinit var binding: ActivityTrainingSessionBinding
    private val viewModel by viewModels<TrainingSessionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpSwipeToDismiss()
        observeTrainingPlan()
        observeTrainingSessionState()
        viewModel.fetchTrainingPlan()
        Timber.d("LWWW training plan id ${viewModel.trainingPlanId}")
    }

    private fun setUpSwipeToDismiss() {
        binding.swipeDismissLayout.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout?) {
                Timber.d("LWWW dismissed")
                finish()
            }
        })
    }

    private fun observeTrainingPlan() {
        viewModel.trainingPlan.observe(this) {
            when(it) {
                is ResultHandler.Loading -> showProgressBar()
                is ResultHandler.Success -> startTrainingSession(it.value)
                else -> throw IllegalStateException("Unknown state TrainingPlan $it")
            }
        }
    }

    private fun showProgressBar() {
        binding.apply {
            currentTrainingFragmentContainer.visibility = View.GONE
            loadingView.loadingLayout.visibility = View.VISIBLE
        }
    }

    private fun startTrainingSession(trainingPlan: TrainingPlan) {
        binding.apply {
            currentTrainingFragmentContainer.visibility = View.VISIBLE
            loadingView.loadingLayout.visibility = View.GONE
        }
        viewModel.startTrainingSession(trainingPlan)
    }

    private fun observeTrainingSessionState() {
        viewModel.trainingSessionState.observe(this) {
            when(it) {
                is TrainingSessionState.ExerciseState -> showCurrentExercise(it)
                is TrainingSessionState.RestTimeState -> showRestTime(it)
                is TrainingSessionState.SummaryState -> showTrainingSessionSummary(it)
                is TrainingSessionState.IdleState -> throw IllegalStateException("Wrong training session state $it")
            }
        }
    }

    private fun showCurrentExercise(state: TrainingSessionState.ExerciseState) {
        Timber.d("LWWW show current exercise fragment")
        val fragment = TrainingExerciseFragment.newInstance()
        replaceFragment(fragment)
    }

    private fun showRestTime(state: TrainingSessionState.RestTimeState) {
        Timber.d("LWWW show rest time fragment")
        val fragment = TrainingRestTimeFragment.newInstance()
        replaceFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.current_training_fragment_container, fragment)
        }
    }

    private fun showTrainingSessionSummary(state: TrainingSessionState.SummaryState) {
//        TODO("Not yet implemented")
    }

}
