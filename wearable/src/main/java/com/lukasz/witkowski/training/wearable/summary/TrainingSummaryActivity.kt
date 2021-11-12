package com.lukasz.witkowski.training.wearable.summary

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.wearable.R
import com.lukasz.witkowski.training.wearable.databinding.ActivityTrainingSummaryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingSummaryActivity : ComponentActivity() {

    companion object {
        const val TRAINING_TIME_KEY = "trainingTime"
    }

    private lateinit var binding: ActivityTrainingSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val trainingTime = intent.extras?.getLong(TRAINING_TIME_KEY) ?: 0L
        binding.totalTimeTv.text = getString(R.string.total_training_time, TimeFormatter.millisToTime(trainingTime))
        binding.exitTrainingBtn.setOnClickListener {
            finish()
        }
    }
}