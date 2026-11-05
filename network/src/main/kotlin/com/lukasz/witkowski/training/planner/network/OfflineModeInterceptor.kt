package com.lukasz.witkowski.training.planner.network

import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlagProvider
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class OfflineModeException :
    IOException("Network requests are disabled by FeatureFlag.ONLINE_SYNC")

class OfflineModeInterceptor(private val featureFlagProvider: FeatureFlagProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!featureFlagProvider.isEnabled(FeatureFlag.ONLINE_SYNC)) {
            Timber.d(
                "Blocking outgoing HTTP request to %s: ONLINE_SYNC is disabled",
                chain.request().url
            )
            throw OfflineModeException()
        }
        return chain.proceed(chain.request())
    }
}
