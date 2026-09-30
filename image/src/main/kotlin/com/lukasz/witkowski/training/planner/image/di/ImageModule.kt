package com.lukasz.witkowski.training.planner.image.di

import com.lukasz.witkowski.training.planner.image.DataReferenceSeparatedImageStorage
import com.lukasz.witkowski.training.planner.image.ImageStorage
import com.lukasz.witkowski.training.planner.image.domain.ChecksumCalculator
import com.lukasz.witkowski.training.planner.image.domain.ImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.domain.ImageRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.Adler32ChecksumCalculator
import com.lukasz.witkowski.training.planner.image.infrastructure.DbImageReferenceRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.InternalStorageImageRepository
import com.lukasz.witkowski.training.planner.image.infrastructure.db.ImageReferenceDatabase
import com.lukasz.witkowski.training.planner.shared.di.CoroutineDispatcherQualifiers
import com.lukasz.witkowski.training.planner.shared.di.dispatchersModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val imageModule = module {
    includes(dispatchersModule)
    val directoryName = "TrainingPlannerImageStorage"
    single {
        InternalStorageImageRepository(
            context = androidContext(),
            directoryName = directoryName,
            ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO))
        )
    } bind ImageRepository::class

    single { ImageReferenceDatabase.getInstance(androidContext()) }
    single { get<ImageReferenceDatabase>().imageReferenceDao() }
    single {
        DbImageReferenceRepository(
            imageReferenceDao = get(),
            ioDispatcher = get(named(CoroutineDispatcherQualifiers.IO))
        )
    } bind ImageReferenceRepository::class

    single<Adler32ChecksumCalculator>() bind ChecksumCalculator::class
    single<DataReferenceSeparatedImageStorage>() bind ImageStorage::class
}
