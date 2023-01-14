package com.lukasz.witkowski.training.planner

import android.content.Context
import com.lukasz.witkowski.training.planner.training.di.TrainingContainer

class AppContainer(private val context: Context) {
    val trainingContainer = TrainingContainer(context)
}
