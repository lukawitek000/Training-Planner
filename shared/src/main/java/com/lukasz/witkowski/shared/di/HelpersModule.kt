package com.lukasz.witkowski.shared.di

import com.lukasz.witkowski.shared.currentTraining.TimerHelper
import com.lukasz.witkowski.shared.currentTraining.TrainingProgressController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HelpersModule {

    @Singleton
    @Provides
    fun provideTimerHelper(): TimerHelper {
        return TimerHelper()
    }

    @Singleton
    @Provides
    fun provideTrainingProgressController(): TrainingProgressController {
        return TrainingProgressController()
    }
}