package com.lukasz.witkowski.training.planner.traininglist

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.wear.widget.WearableLinearLayoutManager
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.WearableTrainingPlannerViewModelFactory
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingPlansListBinding
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity.Companion.TRAINING_ID_KEY
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity.Companion.TRAINING_TITLE_KEY
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import timber.log.Timber

class TrainingsListActivity : ComponentActivity() {

    private lateinit var binding: ActivityTrainingPlansListBinding
    private lateinit var adapter: TrainingPlansAdapter
    private val viewModel: TrainingPlansListViewModel by viewModels(factoryProducer = {
        WearableTrainingPlannerViewModelFactory()
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingPlansListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpTrainingAdapter()
        observeFetchedTrainingPlans()
        fetchTrainingPlans()
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
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
        intent.putExtra(TRAINING_ID_KEY, trainingId.toString())
        intent.putExtra(TRAINING_TITLE_KEY, trainingTitle)
        startActivity(intent)
    }

    private fun fetchTrainingPlans() {
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

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            Timber.d("All required permissions granted")
        } else {
            Timber.w("Not all required permissions granted")
            Toast.makeText(
                this,
                "Permissions are required to collect body condition data",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    }
}
