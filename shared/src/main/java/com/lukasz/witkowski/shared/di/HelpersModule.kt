package com.lukasz.witkowski.shared.di

import com.lukasz.witkowski.shared.trainingControllers.TimerHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object HelpersModule {

    @ServiceScoped
    @Provides
    fun provideTimerHelper(): TimerHelper {
        return TimerHelper()
    }

//    @ServiceScoped
//    @Provides
//    fun provideTrainingProgressController(): TrainingProgressController {
//        return TrainingProgressController()
//    }
}