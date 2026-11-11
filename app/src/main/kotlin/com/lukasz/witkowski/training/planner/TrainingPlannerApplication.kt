package com.lukasz.witkowski.training.planner

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.lukasz.witkowski.training.planner.di.appModule
import com.lukasz.witkowski.training.planner.image.infrastructure.ThemeAwareSvgDecoder
import com.lukasz.witkowski.training.planner.network.SyncScheduler
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlagProvider
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class TrainingPlannerApplication : Application(), SingletonImageLoader.Factory {
    private val syncScheduler: SyncScheduler by inject()
    private val featureFlagProvider: FeatureFlagProvider by inject()

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(ThemeAwareSvgDecoder.Factory())
            }
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidContext(this@TrainingPlannerApplication)
            modules(appModule)
        }
        if (featureFlagProvider.isEnabled(FeatureFlag.ONLINE_SYNC)) {
            syncScheduler.enqueueRemoteChangeSync(force = false)
            syncScheduler.enqueueMutationSync()
        } else {
            syncScheduler.cancelMutationSync()
        }
    }
}
