package com.lukasz.witkowski.training.planner.shared.time

import androidx.annotation.StringRes

internal interface ResourcesProvider {
    fun provideString(@StringRes resId: Int): String
}
