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
        val dbExercise = exerciseDao.getById(id.value.toString())
        ExerciseMapper.toExercise(dbExercise)
    }

    override fun getAll(): Flow<List<Exercise>> {
        return exerciseDao.getAll()
            .map { it.map { dbExercise -> ExerciseMapper.toExercise(dbExercise) } }
    }

    override suspend fun insert(exercise: Exercise): Boolean = withContext(ioDispatcher) {
        val exerciseWithImage = ExerciseMapper.toDbExercise(exercise)
        exerciseDao.insert(exerciseWithImage) == ONE_ROW.toLong()
    }

    override suspend fun delete(exercise: Exercise) = withContext(ioDispatcher) {
        exerciseDao.deleteExerciseById(exercise.id.value.toString()) == ONE_ROW
    }

    override suspend fun updateExercise(updatedExercise: Exercise): Boolean =
        withContext(ioDispatcher) {
            val dbExercise = ExerciseMapper.toDbExercise(updatedExercise)
            exerciseDao.update(dbExercise) == ONE_ROW
        }

    private companion object {
        const val ONE_ROW = 1
    }
}
