package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DbExerciseRepository(private val exerciseDao: ExerciseDao) : ExerciseRepository {
    override fun getById(id: ExerciseId): Flow<Exercise> {
        return exerciseDao.getById(id.value).map { ExerciseMapper.toExercise(it)  }
    }

    override fun getAll(): Flow<List<Exercise>> {
        return exerciseDao.getAll().map { it.map { dbExercise -> ExerciseMapper.toExercise(dbExercise) } }
    }

    override suspend fun insert(exercise: Exercise): Boolean {
        val dbExercise = ExerciseMapper.toDbExercise(exercise)
        exerciseDao.insert(dbExercise)
        return true
    }

    override suspend fun delete(exercise: Exercise) {
        exerciseDao.delete(exercise.id.value)
    }

    override suspend fun updateExercise(updatedExercise: Exercise): Boolean {
        val dbExercise = ExerciseMapper.toDbExercise(updatedExercise)
        return exerciseDao.update(dbExercise) == SINGLE_ENTRY_UPDATE
    }

    override suspend fun getExerciseId(
        name: String,
        description: String,
        category: ExerciseCategory
    ): ExerciseId? {
        val id = exerciseDao.getExerciseId(name, description, category.ordinal)
        return id?.let { ExerciseId(it) }
    }

    companion object {
        private const val SINGLE_ENTRY_UPDATE = 1
    }
}
