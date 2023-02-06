package com.lukasz.witkowski.training.planner

import android.content.Context
import com.lukasz.witkowski.training.planner.exercise.di.ExerciseContainer
import com.lukasz.witkowski.training.planner.statistics.di.StatisticsContainer
import com.lukasz.witkowski.training.planner.training.di.TrainingContainer

class AppContainer(private val context: Context) {
    val trainingContainer = TrainingContainer.getInstance(context)
    val statisticsContainer = StatisticsContainer.getInstance(context)
}
