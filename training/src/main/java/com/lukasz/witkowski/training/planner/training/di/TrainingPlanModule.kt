package com.lukasz.witkowski.training.planner.training.di

import android.content.Context
import androidx.room.Room
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.SendTrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.WearableChannelSendTrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.db.DbTrainingPlanRepository
import com.lukasz.witkowski.training.planner.training.infrastructure.db.TrainingPlanDao
import com.lukasz.witkowski.training.planner.training.infrastructure.db.TrainingPlanDatabase
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
    fun provideTrainingPlanService(trainingPlanRepository: TrainingPlanRepository, sendTrainingPlanRepository: SendTrainingPlanRepository): TrainingPlanService {
        return TrainingPlanService(trainingPlanRepository, sendTrainingPlanRepository)
    }

    @Singleton
    @Provides
    fun provideDbTrainingPlanRepository(trainingPlanDao: TrainingPlanDao): DbTrainingPlanRepository {
        return DbTrainingPlanRepository(trainingPlanDao)
    }

    @Singleton
    @Provides
    fun provideSendTrainingPlanRepository(@ApplicationContext context: Context): SendTrainingPlanRepository {
        return WearableChannelSendTrainingPlanRepository(context)
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