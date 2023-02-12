package com.lukasz.witkowski.training.planner.trainingSession

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.wear.widget.SwipeDismissFrameLayout
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.WearableTrainingPlannerViewModelFactory
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingSessionBinding
import com.lukasz.witkowski.training.planner.session.service.SessionFinishedListener
import com.lukasz.witkowski.training.planner.session.service.SessionServiceConnector
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.summary.TrainingSummaryActivity
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import timber.log.Timber

class TrainingSessionActivity : FragmentActivity() {

    private lateinit var binding: ActivityTrainingSessionBinding
    private val viewModel by viewModels<TrainingSessionViewModel>(factoryProducer = {
        WearableTrainingPlannerViewModelFactory()
    })

    private lateinit var sessionServiceConnector: SessionServiceConnector
    private val sessionFinishedListener = SessionFinishedListener {
        showTrainingSessionSummary(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionServiceConnector = SessionServiceConnector()
        setUpSwipeToDismiss()
        observeTrainingPlan()
        observeTrainingSessionState()
        viewModel.fetchTrainingPlan()
    }

    override fun onStart() {
        super.onStart()

        sessionServiceConnector.addSessionFinishedListener(sessionFinishedListener)
        sessionServiceConnector.bindService(this)
    }

    override fun onStop() {
        super.onStop()
        sessionServiceConnector.removeSessionFinishedListener(sessionFinishedListener)
        sessionServiceConnector.unbindService(this)
    }

    private fun setUpSwipeToDismiss() {
        binding.swipeDismissLayout.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout?) {
                finish()
            }
        })
    }

    private fun observeTrainingPlan() {
        viewModel.trainingPlan.observe(this) {
            when (it) {
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
            when (it) {
                is TrainingSessionState.ExerciseState -> showCurrentExercise()
                is TrainingSessionState.RestTimeState -> showRestTime()
                is TrainingSessionState.SummaryState -> showProgressBar()
                is TrainingSessionState.IdleState -> showProgressBar()
            }
        }
    }

    private fun showCurrentExercise() {
        val fragment = TrainingExerciseFragment.newInstance()
        replaceFragment(fragment)
    }

    private fun showRestTime() {
        val fragment = TrainingRestTimeFragment.newInstance()
        replaceFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.current_training_fragment_container, fragment)
        }
    }

    private fun showTrainingSessionSummary(id: TrainingStatisticsId) {
        val intent = Intent(this, TrainingSummaryActivity::class.java)
        intent.putExtra(TRAINING_STATISTICS_ID, id.value.toString())
        startActivity(intent)
    }

    companion object {
        const val TRAINING_STATISTICS_ID = "TRAINING_STATISTICS_ID"
    }
}
