package com.lukasz.witkowski.training.planner.training.di

import android.content.Context
import androidx.room.Room
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanReceiver
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanSender
import com.lukasz.witkowski.training.planner.training.infrastructure.db.DbTrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.db.TrainingPlanDao
import com.lukasz.witkowski.training.planner.training.infrastructure.db.TrainingPlanDatabase
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.WearableTrainingPlanReceiver
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.WearableTrainingPlanSender
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object TrainingPlanModule {

    @Singleton
    @Provides
    fun provideTrainingPlanService(
        trainingPlanRepository: TrainingPlanRepository,
        trainingPlanSender: TrainingPlanSender,
        trainingPlanReceiver: TrainingPlanReceiver
    ): TrainingPlanService {
        return TrainingPlanService(trainingPlanRepository, trainingPlanSender, trainingPlanReceiver)
    }

    @Singleton
    @Provides
    fun provideDbTrainingPlanRepository(trainingPlanDao: TrainingPlanDao): DbTrainingPlanRepository {
        return DbTrainingPlanRepository(trainingPlanDao)
    }

    @Singleton
    @Provides
    fun provideTrainingPlanSender(@ApplicationContext context: Context): TrainingPlanSender {
        return WearableTrainingPlanSender(context)
    }

    @Singleton
    @Provides
    fun provideTrainingPlanReceiver(): TrainingPlanReceiver {
        return WearableTrainingPlanReceiver()
    }

    @Singleton
    @Provides
    fun provideTrainingPlanDatabase(@ApplicationContext context: Context): TrainingPlanDatabase {
        return Room.databaseBuilder(
            context,
            TrainingPlanDatabase::class.java,
            "Training Plan Database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideTrainingPlanDao(trainingPlanDatabase: TrainingPlanDatabase): TrainingPlanDao {
        return trainingPlanDatabase.trainingPlanDao()
    }
}