package com.lukasz.witkowski.shared.di

import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.shared.repository.SyncDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideSyncDataRepository(trainingDao: TrainingDao): SyncDataRepository {
        return SyncDataRepository(trainingDao = trainingDao)
    }
}
