package com.lukasz.witkowski.training.wearable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.lukasz.witkowski.training.wearable.databinding.ActivityTrainingSummaryBinding

class TrainingSummaryActivity : ComponentActivity() {

    private lateinit var binding: ActivityTrainingSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.exitTrainingBtn.setOnClickListener {
            finish()
        }
    }
}