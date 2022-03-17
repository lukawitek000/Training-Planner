package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DbExerciseRepository(private val exerciseDao: ExerciseDao) : ExerciseRepository {
    override fun getById(id: String): Flow<Exercise> {
        return exerciseDao.getById(id).map { ExerciseMapper.toExercise(it)  }
    }

    override fun getAll(): Flow<List<Exercise>> {
        return exerciseDao.getAll().map { it.map { dbExercise -> ExerciseMapper.toExercise(dbExercise) } }
    }

    override suspend fun insert(exercise: Exercise) : Long {
        val dbExercise = ExerciseMapper.toDbExercise(exercise)
        return exerciseDao.insert(dbExercise)
    }

    override suspend fun delete(exercise: Exercise) {
        val dbExercise = ExerciseMapper.toDbExercise(exercise)
        exerciseDao.delete(dbExercise)
    }
}