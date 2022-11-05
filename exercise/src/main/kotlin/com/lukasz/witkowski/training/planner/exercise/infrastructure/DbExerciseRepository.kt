package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class DbExerciseRepository(private val exerciseDao: ExerciseDao) : ExerciseRepository {

    override suspend fun getById(id: ExerciseId): Exercise = withContext(Dispatchers.IO) {
        val dbExercise = exerciseDao.getById(id.value)
        ExerciseMapper.toExercise(dbExercise)
    }

    override fun getAll(): Flow<List<Exercise>> {
        return exerciseDao.getAll()
            .map { it.map { dbExercise -> ExerciseMapper.toExercise(dbExercise) } }
    }

    override suspend fun insert(exercise: Exercise): Boolean {
        val exerciseWithImage = ExerciseMapper.toDbExercise(exercise)
        exerciseDao.insert(exerciseWithImage)
        return true
    }

    override suspend fun delete(exercise: Exercise) {
        exerciseDao.deleteExerciseById(exercise.id.value)
    }

    override suspend fun updateExercise(updatedExercise: Exercise): Boolean {
        val dbExercise = ExerciseMapper.toDbExercise(updatedExercise)
        return exerciseDao.update(dbExercise) == ONE_ROW
    }

    private companion object {
        const val ONE_ROW = 1
    }
}
