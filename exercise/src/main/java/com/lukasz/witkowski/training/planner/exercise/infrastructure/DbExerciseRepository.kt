package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
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
        val exerciseWithImage = ExerciseMapper.toExerciseWithImage(exercise)
        exerciseDao.insert(exerciseWithImage)
        return true
    }

    override suspend fun delete(exercise: Exercise) {
        exerciseDao.deleteExerciseWithImage(exercise.id.value)
    }

    override suspend fun updateExercise(updatedExercise: Exercise): Boolean {
        val exerciseWithImage = ExerciseMapper.toExerciseWithImage(updatedExercise)
        return exerciseDao.update(exerciseWithImage)
    }
}
