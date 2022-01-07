package com.lukasz.witkowski.training.planner.summary

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.currentTraining.service.TrainingService
import com.lukasz.witkowski.training.planner.databinding.ActivityTrainingSummaryBinding
import com.lukasz.witkowski.training.planner.trainingsList.TrainingsListActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingSummaryActivity : ComponentActivity() {

    companion object {
        const val TRAINING_TIME_KEY = "trainingTime"
    }

    private lateinit var binding: ActivityTrainingSummaryBinding

    private lateinit var trainingService: TrainingService

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as TrainingService.LocalBinder).getService()

        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val trainingTime = intent.extras?.getLong(TRAINING_TIME_KEY) ?: 0L
        binding.totalTimeTv.text = TimeFormatter.millisToTime(trainingTime)
        binding.exitTrainingBtn.setOnClickListener {
            val intent = Intent(this, TrainingsListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}