package com.lukasz.witkowski.training.planner.trainingplans

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.wear.widget.WearableLinearLayoutManager
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.shared.utils.startSendingDataService
import com.lukasz.witkowski.shared.utils.stopSendingDataService
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingsListBinding
import com.lukasz.witkowski.training.planner.service.SendingStatisticsService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan
import com.lukasz.witkowski.training.planner.ui.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.ui.startTraining.StartTrainingActivity.Companion.TRAINING_ID_KEY
import com.lukasz.witkowski.training.planner.ui.startTraining.StartTrainingActivity.Companion.TRAINING_TITLE_KEY
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TrainingsListActivity : ComponentActivity() {

    private lateinit var binding: ActivityTrainingsListBinding
    private lateinit var adapter: TrainingsAdapter
    private val viewModel: TrainingsListViewModel by viewModels()
    private var isServiceStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpTrainingAdapter()
        getTrainings()
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    override fun onStart() {
        super.onStart()
        if (!isServiceStarted) {
            isServiceStarted = startSendingDataService(SendingStatisticsService::class.java)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isServiceStarted) {
            isServiceStarted = stopSendingDataService(SendingStatisticsService::class.java)
        }
    }

    private fun setUpTrainingAdapter() {
        adapter = TrainingsAdapter(context = this) { id, title ->
            navigateToStartTrainingActivity(id, title)
        }

        binding.trainingsWatchRv.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@TrainingsListActivity)
            adapter = this@TrainingsListActivity.adapter
        }
    }

    private fun navigateToStartTrainingActivity(trainingId: TrainingPlanId, trainingTitle: String) {
        val intent = Intent(this, StartTrainingActivity::class.java)
        intent.putExtra(TRAINING_ID_KEY, trainingId.value)
        intent.putExtra(TRAINING_TITLE_KEY, trainingTitle)
        startActivity(intent)
    }

    private fun getTrainings() {
        observeFetchedTrainings()
        viewModel.getTrainingsWithExercises()
    }

    private fun observeFetchedTrainings() {
        viewModel.trainings.observe(this) {
            when (it) {
                is ResultHandler.Success -> setTrainingsListToAdapter(it.value)
                is ResultHandler.Loading -> setLoadingState()
                is ResultHandler.Error -> handleFetchingDataError(it.cause)
                else -> {}
            }
        }
    }

    private fun handleFetchingDataError(cause: Exception?) {
        Timber.w("Fetching data from DB failed ${cause?.localizedMessage}")
        Toast.makeText(this, "Fetching data from database failed", Toast.LENGTH_SHORT).show()
    }

    private fun setLoadingState() {
        binding.loadingView.loadingLayout.visibility = View.VISIBLE
        binding.noTrainingsMessage.visibility = View.GONE
    }

    private fun setTrainingsListToAdapter(data: List<TrainingPlan>) {
        binding.loadingView.loadingLayout.visibility = View.GONE
        binding.noTrainingsMessage.visibility  = if (data.isEmpty()) View.VISIBLE else View.GONE
        adapter.submitList(data)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            Timber.d("All required permissions granted")
        } else {
            Timber.w("Not all required permissions granted")
        }
    }

    private companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    }
}
