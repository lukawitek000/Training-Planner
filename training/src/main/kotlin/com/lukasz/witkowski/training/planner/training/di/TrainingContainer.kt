package com.lukasz.witkowski.training.planner.training.di

import android.content.Context
import androidx.room.Room
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.infrastructure.db.DbTrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.db.TrainingPlanDatabase
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.WearableTrainingPlanSender

class TrainingContainer(private val context: Context) {

    private val trainingPlanDb = Room.databaseBuilder(
        context,
        TrainingPlanDatabase::class.java,
        "Training Plan Database"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val trainingPlanRepository: TrainingPlanRepository by lazy {
        DbTrainingPlanRepository(trainingPlanDb.trainingPlanDao())
    }

    private val trainingPlanSender: TrainingPlanSender by lazy {
        WearableTrainingPlanSender(context)
    }

    val service: TrainingPlanService by lazy {
        TrainingPlanService(trainingPlanRepository, trainingPlanSender)
    }
}