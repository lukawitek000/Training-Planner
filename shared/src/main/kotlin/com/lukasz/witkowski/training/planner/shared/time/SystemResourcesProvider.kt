package com.lukasz.witkowski.training.planner.shared.time

import android.content.Context
import androidx.annotation.StringRes

internal class SystemResourcesProvider(private val context: Context) : ResourcesProvider {
    override fun provideString(@StringRes resId: Int): String = context.getString(resId)
}
