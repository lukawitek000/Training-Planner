package com.lukasz.witkowski.training.planner.image.di

import com.lukasz.witkowski.training.planner.image.DataReferenceSeparatedImageStorage
import com.lukasz.witkowski.training.planner.image.ImageStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ImageStorageModule {

    @Binds
    abstract fun bindImageStorage(
        internalImageStorage: DataReferenceSeparatedImageStorage
    ): ImageStorage
}
