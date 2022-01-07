package com.lukasz.witkowski.training.planner.trainingsList

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.coroutineScope
import androidx.wear.widget.WearableLinearLayoutManager
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity.Companion.TRAINING_ID_KEY
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity.Companion.TRAINING_TITLE_KEY
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingsListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TrainingsListActivity : ComponentActivity() {

    private lateinit var binding: ActivityTrainingsListBinding
    private lateinit var adapter: TrainingsAdapter
    private val viewModel : TrainingsListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrainingsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpTrainingAdapter()
        getTrainings()
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun getTrainings() {
        // TODO observe trainings returned from the DB
        lifecycle.coroutineScope.launch {
            viewModel.trainings.collect() {
                if(it.isEmpty()){
                    binding.noTrainingsMessage.visibility = View.VISIBLE
                } else {
                    binding.noTrainingsMessage.visibility = View.GONE
                }
                adapter.submitList(it)
            }
        }
    }

    private fun setUpTrainingAdapter() {
        adapter = TrainingsAdapter() { id, title ->
            navigateToStartTrainingActivity(id, title)
        }

        binding.trainingsWatchRv.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@TrainingsListActivity)
            adapter = this@TrainingsListActivity.adapter
        }
    }

    private fun navigateToStartTrainingActivity(trainingId: Long, trainingTitle: String) {
        val intent = Intent(this, StartTrainingActivity::class.java)
        intent.putExtra(TRAINING_ID_KEY, trainingId)
        intent.putExtra(TRAINING_TITLE_KEY, trainingTitle)
        startActivity(intent)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            Timber.d( "All required permissions granted")
        } else {
            Timber.w( "Not all required permissions granted")
        }
    }

    private companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    }
}
