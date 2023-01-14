package com.lukasz.witkowski.training.planner.training.di

import android.content.Context
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.infrastructure.db.DbTrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.db.TrainingPlanDatabase
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.WearableTrainingPlanSender

class TrainingContainer private constructor(context: Context) {

    private val trainingPlanRepository: TrainingPlanRepository by lazy {
        val trainingPlanDb = TrainingPlanDatabase.getInstance(context)
        DbTrainingPlanRepository(trainingPlanDb.trainingPlanDao())
    }

    private val trainingPlanSender: TrainingPlanSender by lazy {
        WearableTrainingPlanSender(context)
    }

    val service: TrainingPlanService by lazy {
        TrainingPlanService(trainingPlanRepository, trainingPlanSender)
    }

    companion object {
        @Volatile
        private var instance: TrainingContainer? = null

        fun getInstance(context: Context): TrainingContainer {
            return synchronized(this) {
                if (instance == null) {
                    instance = TrainingContainer(context)
                }
                instance!!
            }
        }
    }
}
