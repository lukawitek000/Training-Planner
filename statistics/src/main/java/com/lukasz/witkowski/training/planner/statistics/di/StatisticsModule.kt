package com.lukasz.witkowski.training.planner.statistics.di

import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.statistics.presentation.DefaultTimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.DefaultTrainingSessionController
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object StatisticsModule {

    @Provides
    fun provideTrainingSessionService(): TrainingSessionService {
        return TrainingSessionService()
    }

    @Provides
    fun provideTrainingSessionController(trainingSessionService: TrainingSessionService): TrainingSessionController {
        return DefaultTrainingSessionController(trainingSessionService)
    }

    @Provides
    fun provideTimerController(): TimerController {
        return DefaultTimerController()
    }
}