package com.lukasz.witkowski.training.planner.summary

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.lukasz.witkowski.training.planner.R
import dagger.hilt.android.AndroidEntryPoint

// TODO most probably to pass data it will be convenient to save it to db and load it in this activity
@AndroidEntryPoint
class TrainingSummaryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
    }
}
