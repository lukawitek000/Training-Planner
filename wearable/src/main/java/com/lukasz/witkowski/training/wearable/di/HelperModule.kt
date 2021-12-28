package com.lukasz.witkowski.training.wearable.di

import com.lukasz.witkowski.training.wearable.currentTraining.service.CurrentTrainingProgressHelper
import com.lukasz.witkowski.training.wearable.repo.CurrentTrainingRepository
import com.lukasz.witkowski.training.wearable.repo.TrainingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object HelperModule {

//    @Singleton
//    @Provides
//    fun provideCurrentTrainingProgressHelper(): CurrentTrainingProgressHelper {
//        return CurrentTrainingProgressHelper()
//    }
}