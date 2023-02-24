package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class DbExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) : ExerciseRepository {

    override suspend fun getById(id: ExerciseId): Exercise = withContext(ioDispatcher) {
        val dbExercise = exerciseDao.getById(id.toString())
        dbExercise.toExercise()
    }

    override fun getAll(): Flow<List<Exercise>> {
        return exerciseDao.getAll()
            .map { it.map { dbExercise -> dbExercise.toExercise() } }
    }

    override suspend fun insert(exercise: Exercise): Boolean = withContext(ioDispatcher) {
        val exerciseWithImage = exercise.toDbExercise()
        exerciseDao.insert(exerciseWithImage) == ONE_ROW.toLong()
    }

    override suspend fun delete(exercise: Exercise) = withContext(ioDispatcher) {
        exerciseDao.deleteExerciseById(exercise.id.toString()) == ONE_ROW
    }

    override suspend fun updateExercise(updatedExercise: Exercise): Boolean =
        withContext(ioDispatcher) {
            val dbExercise = updatedExercise.toDbExercise()
            exerciseDao.update(dbExercise) == ONE_ROW
        }

    private companion object {
        const val ONE_ROW = 1
    }
}
