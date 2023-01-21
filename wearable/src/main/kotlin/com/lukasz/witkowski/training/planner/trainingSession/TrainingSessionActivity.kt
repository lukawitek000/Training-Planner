package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.wear.widget.SwipeDismissFrameLayout
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingSessionBinding
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingViewModel
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
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

    private fun startTrainingSession(trainingPlan: TrainingPlan) {
        Timber.d("LWWW start training session with $trainingPlan")
    }

    private fun showProgressBar() {
        Timber.d("LWWW show progress bar")
    }

}
