package com.lukasz.witkowski.training.planner.summary

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingSummaryBinding
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.utils.launchInStartedState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrainingSummaryActivity : ComponentActivity() {

    private lateinit var binding: ActivityTrainingSummaryBinding
    private val viewModel by viewModels<TrainingSummaryViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchSummary()
    }

    private fun fetchSummary() {
        launchInStartedState {
            viewModel.loadTrainingStatistics().collectLatest {
                populateUi(it)
            }
        }
    }

    private fun populateUi(trainingStatistics: TrainingStatistics) {
        binding.apply {
            summaryContentLayout.visibility = View.VISIBLE
            loadingStatisticsPb.visibility = View.GONE
            totalTimeTv.text = trainingStatistics.totalTime.toString()
//            burnedCaloriesTv.text = trainingStatistics.id.toString()
        }
    }
}
