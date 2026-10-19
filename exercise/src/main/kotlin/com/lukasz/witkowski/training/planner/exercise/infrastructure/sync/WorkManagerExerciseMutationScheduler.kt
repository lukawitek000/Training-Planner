package com.lukasz.witkowski.training.planner.exercise.infrastructure.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMutationScheduler
import timber.log.Timber

class WorkManagerExerciseMutationScheduler(
    private val context: Context
) : ExerciseMutationScheduler {

    override fun enqueueMutationSync() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<ExerciseMutationWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Timber.d("Enqueued ExerciseMutationWorker with CONNECTED network constraint")
        } catch (e: Exception) {
            Timber.w(e, "Failed to enqueue ExerciseMutationWorker")
        }
    }

    companion object {
        const val WORKER_NAME = "ExerciseMutationWorker"
    }
}
