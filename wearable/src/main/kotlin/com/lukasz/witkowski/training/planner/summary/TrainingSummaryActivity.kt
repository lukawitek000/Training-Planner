package com.lukasz.witkowski.training.planner.summary

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.WearableTrainingPlannerViewModelFactory
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingSummaryBinding
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.traininglist.TrainingsListActivity
import com.lukasz.witkowski.training.planner.utils.launchInStartedState
import kotlinx.coroutines.flow.collectLatest

class TrainingSummaryActivity : ComponentActivity() {

    private lateinit var binding: ActivityTrainingSummaryBinding
    private val viewModel by viewModels<TrainingSummaryViewModel>(factoryProducer = {
        WearableTrainingPlannerViewModelFactory()
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchSummary()
        setUpExitButton()
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
            totalTimeSv.setStatisticValue(trainingStatistics.totalTime.toString())
        }
    }

    private fun setUpExitButton() {
        binding.exitTrainingBtn.setOnClickListener {
            val intent = Intent(this, TrainingsListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}
