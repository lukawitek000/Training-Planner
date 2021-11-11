package com.lukasz.witkowski.training.wearable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.lukasz.witkowski.training.wearable.databinding.ActivityTrainingExerciseBinding

class TrainingExerciseActivity : ComponentActivity() {

    private lateinit var binding: ActivityTrainingExerciseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}