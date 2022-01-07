package com.lukasz.witkowski.training.planner.summary

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.currentTraining.service.TrainingService
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingSummaryBinding
import com.lukasz.witkowski.training.planner.trainingsList.TrainingsListActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TrainingSummaryActivity : ComponentActivity() {

    private lateinit var binding: ActivityTrainingSummaryBinding

    private lateinit var trainingService: TrainingService

    private val viewModel by viewModels<TrainingSummaryViewModel>()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as TrainingService.LocalBinder).getService()
            observeInsertingStatistics()
            viewModel.trainingId = trainingService.trainingId
            displaySummaryProperties()
            trainingService.trainingCompleteStatistics?.let { viewModel.insertTrainingStatistics(it) }

        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val serviceIntent = Intent(this, TrainingService::class.java)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        binding.exitTrainingBtn.setOnClickListener {
            val intent = Intent(this, TrainingsListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    private fun observeInsertingStatistics() {
        viewModel.statisticsId.observe(this) {
            Timber.d("Statistics inserted")
            trainingService.stopCurrentService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    private fun displaySummaryProperties() {
        binding.totalTimeTv.text =
            TimeFormatter.millisToTime(trainingService.currentTrainingProgressHelper.trainingTime)
        var totalBurnedCalories = 0.0
        trainingService.trainingCompleteStatistics?.exercisesStatistics?.forEach {
            totalBurnedCalories += it.burntCaloriesStatistics.burntCalories
        }
        binding.burnedCaloriesTv.text =
            getString(R.string.total_burned_calories, totalBurnedCalories)
        val maxHeartRate = trainingService.trainingCompleteStatistics?.exercisesStatistics?.maxByOrNull {
            it.heartRateStatistics.max
        }?.heartRateStatistics?.max ?: 0.0
        binding.maxHeartRateTv.text = getString(R.string.max_heart_rate, maxHeartRate)
    }
}