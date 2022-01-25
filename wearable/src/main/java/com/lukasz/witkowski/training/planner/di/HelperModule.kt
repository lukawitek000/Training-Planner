package com.lukasz.witkowski.training.planner.di

import android.content.Context
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.HealthServices
import com.lukasz.witkowski.training.planner.ui.currentTraining.TrainingStatisticsRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object HelperModule {

    @ServiceScoped
    @Provides
    fun provideExerciseClient(@ApplicationContext context: Context): ExerciseClient {
        return HealthServices.getClient(context).exerciseClient
    }

    @ServiceScoped
    @Provides
    fun provideTrainingStatisticsRecorder(exerciseClient: ExerciseClient): TrainingStatisticsRecorder {
        return TrainingStatisticsRecorder(exerciseClient)
    }
}