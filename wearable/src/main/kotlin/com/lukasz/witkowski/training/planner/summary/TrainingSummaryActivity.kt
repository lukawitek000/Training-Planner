package com.lukasz.witkowski.training.planner.summary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingSummaryBinding
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import dagger.hilt.android.AndroidEntryPoint

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
        viewModel.trainingStatistics.observe(this) {
            populateUi(it)
        }
        viewModel.loadTrainingStatistics()
    }

    private fun populateUi(trainingStatistics: TrainingStatistics) {
        binding.totalTimeTv.text = trainingStatistics.totalTime.toString()
        binding.burnedCaloriesTv.text = trainingStatistics.id.toString()
    }
}
