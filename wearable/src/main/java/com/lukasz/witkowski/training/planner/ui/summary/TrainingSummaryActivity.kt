package com.lukasz.witkowski.training.planner.ui.summary

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.lukasz.witkowski.training.planner.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingSummaryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
    }
}
