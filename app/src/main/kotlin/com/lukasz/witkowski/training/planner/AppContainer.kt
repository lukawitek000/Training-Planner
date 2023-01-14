package com.lukasz.witkowski.training.planner

import android.content.Context
import com.lukasz.witkowski.training.planner.exercise.di.ExerciseContainer
import com.lukasz.witkowski.training.planner.image.ImageStorage
import com.lukasz.witkowski.training.planner.image.ImageStorageFactory
import com.lukasz.witkowski.training.planner.statistics.di.StatisticsContainer
import com.lukasz.witkowski.training.planner.training.di.TrainingContainer

class AppContainer(private val context: Context) {
    private val imageStorage = ImageStorageFactory.create(context, "TrainingPlannerImageStorage")
    val exerciseContainer = ExerciseContainer(context, imageStorage)
    val trainingContainer = TrainingContainer.getInstance(context)
    val statisticsContainer = StatisticsContainer(context)
}
