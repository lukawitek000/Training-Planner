package com.lukasz.witkowski.training.planner.exercise

import androidx.paging.PagingData
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeExerciseRepository : ExerciseRepository {
    private val _exercises = mutableListOf<ExerciseDetails>()
    val exercises: List<ExerciseDetails>
        get() = _exercises.toList()

    override fun getExerciseDetailsById(id: ExerciseId): Flow<ExerciseDetails?> = flow {
        val customDetails = _exercises.firstOrNull { it.exercise.id == id }
        val exerciseDetails = customDetails ?: TestData.EXERCISE_DETAILS_LIST.firstOrNull {
            it.exercise.id == id
        }?.let { details ->
            details.copy(
                exercise = details.exercise.copy(
                    metadata = ExerciseMetadata(
                        source = ExerciseSource.UserCreated(ExerciseOwner.Local())
                    )
                )
            )
        }
        emit(exerciseDetails)
    }

    override fun queryExercises(query: ExerciseQuery): Flow<PagingData<Exercise2>> = flow {}

    override suspend fun insert(exercise: ExerciseDetails): Boolean = _exercises.add(exercise)

    override suspend fun delete(exerciseId: ExerciseId): Boolean =
        _exercises.removeAll { it.exercise.id == exerciseId }

    override suspend fun updateExercise(exercise: ExerciseDetails): Boolean {
        _exercises.removeAll { it.exercise.id == exercise.exercise.id }
        _exercises.add(exercise)
        return true
    }

    override suspend fun saveExerciseLocally(exerciseDetails: ExerciseDetails): Boolean {
        _exercises.removeAll { it.exercise.id == exerciseDetails.exercise.id }
        _exercises.add(exerciseDetails)
        return true
    }

    override suspend fun resolveConflictKeepLocal(
        exerciseId: ExerciseId,
        latestServerVersion: Long
    ): Boolean = true

    override suspend fun resolveConflictAcceptServer(
        exerciseId: ExerciseId,
        serverExerciseDetails: ExerciseDetails
    ): Boolean = true
}
