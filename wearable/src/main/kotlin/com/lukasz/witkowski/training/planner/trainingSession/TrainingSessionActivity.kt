package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingSessionBinding
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingActivity
import com.lukasz.witkowski.training.planner.startTraining.StartTrainingViewModel
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
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
        Timber.d("LWWW training plan id ${viewModel.trainingPlanId}")
    }

}
