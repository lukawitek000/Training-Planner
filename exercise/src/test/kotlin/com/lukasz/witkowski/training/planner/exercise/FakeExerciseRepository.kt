package com.lukasz.witkowski.training.planner.exercise

import androidx.paging.PagingData
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeExerciseRepository: ExerciseRepository {
    private val _exercises = mutableListOf<ExerciseDetails>()
    val exercises: List<ExerciseDetails>
        get() = _exercises.toList()

    override fun getExerciseDetailsById(id: ExerciseId): Flow<ExerciseDetails> {
        return flow {
            val exerciseDetails = TestData.EXERCISE_DETAILS_LIST.first {
                it.exercise.id == id
            }
            emit(exerciseDetails)
        }
    }

    override fun queryExercises(query: ExerciseQuery): Flow<PagingData<Exercise2>> {
        return flow {}
    }

    override suspend fun insert(exercise: ExerciseDetails): Boolean {
        return _exercises.add(exercise)
    }

    override suspend fun delete(exerciseId: ExerciseId): Boolean {
        return false
    }

    override suspend fun updateExercise(exercise: ExerciseDetails): Boolean {
        val index = _exercises.indexOfFirst { it.exercise.id == exercise.exercise.id }
        if (index != -1) {
            _exercises[index] = exercise
        } else {
            _exercises.add(exercise)
        }
        return true
    }
}