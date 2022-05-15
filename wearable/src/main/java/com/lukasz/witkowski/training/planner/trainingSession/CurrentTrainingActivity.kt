package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.lukasz.witkowski.training.planner.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrentTrainingActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
    }
}
