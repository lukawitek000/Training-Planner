package com.lukasz.witkowski.training.planner.di

import com.lukasz.witkowski.training.planner.BuildConfig
import com.lukasz.witkowski.training.planner.analytics.di.analyticsModule
import com.lukasz.witkowski.training.planner.auth.di.authModule
import com.lukasz.witkowski.training.planner.exercise.di.exerciseModule
import com.lukasz.witkowski.training.planner.network.NetworkQualifiers
import com.lukasz.witkowski.training.planner.navigation.AppNavigationIntentHandler
import com.lukasz.witkowski.training.planner.network.networkModule
import com.lukasz.witkowski.training.planner.notification.AppSessionNotificationContentIntentProvider
import com.lukasz.witkowski.training.planner.session.di.sessionModule
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.SessionNotificationContentIntentProvider
import com.lukasz.witkowski.training.planner.session.infrastructure.notification.SessionWeightUnitProvider
import com.lukasz.witkowski.training.planner.shared.di.timeModule
import com.lukasz.witkowski.training.planner.shared.featureflags.featureFlagsModule
import com.lukasz.witkowski.training.planner.training.di.trainingModule
import com.lukasz.witkowski.training.planner.training.editor.TrainingExerciseConfigurationViewModel
import com.lukasz.witkowski.training.planner.user.di.userModule
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    includes(
        timeModule,
        featureFlagsModule,
        networkModule,
        authModule,
        userModule,
        exerciseModule,
        trainingModule,
        sessionModule,
        analyticsModule
    )

    single<String>(NetworkQualifiers.BASE_URL) {
        BuildConfig.BASE_URL
    }

    single<SessionNotificationContentIntentProvider> {
        AppSessionNotificationContentIntentProvider(context = androidApplication())
    }

    single<SessionWeightUnitProvider> {
        SessionWeightUnitProvider { get<com.lukasz.witkowski.training.planner.user.domain.UserRepository>().weightUnit }
    }

    single {
        AppNavigationIntentHandler(
            trainingSessionService = get()
        )
    }

    viewModel { params ->
        TrainingExerciseConfigurationViewModel(
            service = get(),
            exerciseId = params.get(),
        )
    }
}

