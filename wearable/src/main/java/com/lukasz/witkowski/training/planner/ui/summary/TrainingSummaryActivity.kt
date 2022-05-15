package com.lukasz.witkowski.training.planner.ui.summary

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.shared.utils.startSendingDataService
import com.lukasz.witkowski.shared.utils.stopSendingDataService
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.currentTraining.WearableTrainingService
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingSummaryBinding
import com.lukasz.witkowski.training.planner.trainingplans.TrainingsListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TrainingSummaryActivity : ComponentActivity() {

    private lateinit var binding: ActivityTrainingSummaryBinding
    private lateinit var trainingService: WearableTrainingService
    private var isServiceStarted = false
    private val viewModel by viewModels<TrainingSummaryViewModel>()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as WearableTrainingService.LocalBinder).getService()
//            observeEndedTraining()
//            viewModel.trainingId = trainingService.trainingProgressController.trainingId
        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

    private fun observeEndedTraining() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                trainingService.trainingStatisticsRecorder.isTrainingEnded.collect {
                    if(it) {
                        trainingService.trainingStatisticsRecorder.trainingCompleteStatistics?.let { viewModel.insertTrainingStatistics(it) }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeInsertingStatistics()
        val serviceIntent = Intent(this, WearableTrainingService::class.java)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        binding.exitTrainingBtn.setOnClickListener {
            val intent = Intent(this, TrainingsListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    private fun observeInsertingStatistics() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.insertStatisticsState.collect {
                    when(it) {
                        is ResultHandler.Loading -> showLoadingProgressBar()
                        is ResultHandler.Success -> handleSuccessfulStatisticsRecording()
                        is ResultHandler.Error -> handleError(it.message)
                        is ResultHandler.Idle -> Unit
                    }

                }
            }
        }

    }

    private fun handleError(message: String) {
        Timber.w(message)
        Toast.makeText(this, resources.getString(R.string.save_statistics_failed), Toast.LENGTH_SHORT).show()
        hideProgressBar()
    }

    private fun handleSuccessfulStatisticsRecording() {
        showStatistics()
        hideProgressBar()
        trainingService.stopCurrentService()
        displaySummaryProperties()
    }

    private fun hideProgressBar() {
        binding.loadingStatisticsPb.visibility = View.INVISIBLE
    }

    private fun showStatistics() {
        binding.apply {
            totalTimeIv.visibility = View.VISIBLE
            totalTimeTv.visibility = View.VISIBLE
            maxHeartRateTv.visibility = View.VISIBLE
            maxHeartRateIv.visibility = View.VISIBLE
            burnedCaloriesTv.visibility = View.VISIBLE
            burnedCaloriesIv.visibility = View.VISIBLE
        }
    }

    private fun showLoadingProgressBar() {
        hideStatistics()
        binding.loadingStatisticsPb.visibility = View.VISIBLE
    }

    private fun hideStatistics() {
        binding.apply {
            totalTimeIv.visibility = View.INVISIBLE
            totalTimeTv.visibility = View.INVISIBLE
            maxHeartRateTv.visibility = View.INVISIBLE
            maxHeartRateIv.visibility = View.INVISIBLE
            burnedCaloriesTv.visibility = View.INVISIBLE
            burnedCaloriesIv.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    override fun onStart() {
        super.onStart()
        if(!isServiceStarted) {
//            isServiceStarted = startSendingDataService(SendingStatisticsService::class.java)
        }
    }

    override fun onStop() {
        super.onStop()
        if(isServiceStarted) {
//            isServiceStarted = stopSendingDataService(SendingStatisticsService::class.java)
        }
    }

    private fun displaySummaryProperties() {
        binding.totalTimeTv.text = viewModel.getTrainingTotalTime()
        val totalBurnedCalories = viewModel.calculateTotalBurnedCalories()
        binding.burnedCaloriesTv.text =
            getString(R.string.total_burned_calories, totalBurnedCalories)
        val maxHeartRate = viewModel.calculateMaxHeartRate()
        binding.maxHeartRateTv.text = getString(R.string.max_heart_rate, maxHeartRate)
        hideEmptyHealthStatistics(totalBurnedCalories, maxHeartRate)
    }

    private fun hideEmptyHealthStatistics(totalBurnedCalories: Double, maxHeartRate: Double) {
        if(maxHeartRate == 0.0) {
            binding.maxHeartRateTv.visibility = View.GONE
            binding.maxHeartRateIv.visibility = View.GONE
        }
        if(totalBurnedCalories == 0.0) {
            binding.burnedCaloriesTv.visibility = View.GONE
            binding.burnedCaloriesIv.visibility = View.GONE
        }
    }
}
