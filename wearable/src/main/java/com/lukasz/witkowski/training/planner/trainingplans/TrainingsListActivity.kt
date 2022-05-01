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
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingPlansListBinding
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

    private lateinit var binding: ActivityTrainingPlansListBinding
    private lateinit var adapter: TrainingPlansAdapter
    private val viewModel: TrainingPlansListViewModel by viewModels()
    private var isServiceStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingPlansListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpTrainingAdapter()
        fetchTrainingPlans()
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
        adapter = TrainingPlansAdapter(context = this) { id, title ->
            navigateToStartTrainingActivity(id, title)
        }

        binding.trainingPlansWatchRv.apply {
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

    private fun fetchTrainingPlans() {
        observeFetchedTrainingPlans()
        viewModel.getTrainingPlans()
    }

    private fun observeFetchedTrainingPlans() {
        viewModel.trainingPlans.observe(this) {
            when (it) {
                is ResultHandler.Success -> setTrainingsListToAdapter(it.value)
                is ResultHandler.Loading -> setLoadingState()
                is ResultHandler.Error -> handleFetchingDataError(it.cause)
                is ResultHandler.Idle -> Unit
            }
        }
    }

    private fun handleFetchingDataError(cause: Exception?) {
        Timber.w("Fetching training plans failed: ${cause?.localizedMessage}")
        Toast.makeText(this, getString(R.string.fetching_training_plans_failed), Toast.LENGTH_SHORT)
            .show()
        setTrainingsListToAdapter(emptyList())
    }

    private fun setLoadingState() {
        binding.run {
            loadingView.loadingLayout.visibility = View.VISIBLE
            noTrainingsMessage.visibility = View.GONE
            trainingPlansWatchRv.visibility = View.GONE
        }
    }

    private fun setTrainingsListToAdapter(data: List<TrainingPlan>) {
        binding.run {
            loadingView.loadingLayout.visibility = View.GONE
            noTrainingsMessage.visibility = if (data.isEmpty()) {
                View.VISIBLE
            } else {
                trainingPlansWatchRv.visibility = View.VISIBLE
                View.GONE
            }
        }
        adapter.submitList(data)
    }

    // TODO handle in ui denied permissions, and do not allow later to use this sensors if they are denied
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
