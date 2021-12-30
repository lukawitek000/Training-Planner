package com.lukasz.witkowski.training.wearable.di

import android.content.Context
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.HealthServices
import com.lukasz.witkowski.training.wearable.currentTraining.service.CurrentTrainingProgressHelper
import com.lukasz.witkowski.training.wearable.repo.CurrentTrainingRepository
import com.lukasz.witkowski.training.wearable.repo.TrainingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object HelperModule {

    @Singleton
    @Provides
    fun provideExerciseClient(@ApplicationContext context: Context): ExerciseClient {
        return HealthServices.getClient(context).exerciseClient
    }
}