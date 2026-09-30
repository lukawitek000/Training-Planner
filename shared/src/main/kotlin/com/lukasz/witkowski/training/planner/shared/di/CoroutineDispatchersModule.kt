package com.lukasz.witkowski.training.planner.shared.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

object CoroutineDispatcherQualifiers {
    const val IO = "io"
    const val DEFAULT = "default"
    const val MAIN = "main"
}

val dispatchersModule = module {
    single<CoroutineDispatcher>(named(CoroutineDispatcherQualifiers.IO)) {
        Dispatchers.IO
    }
    single<CoroutineDispatcher>(named(CoroutineDispatcherQualifiers.DEFAULT)) {
        Dispatchers.Default
    }
    single<CoroutineDispatcher>(named(CoroutineDispatcherQualifiers.MAIN)) {
        Dispatchers.Main.immediate
    }
    single<CoroutineDispatcher> {
        Dispatchers.IO
    }
}
