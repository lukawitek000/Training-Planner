package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

internal class DbExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO,
) : ExerciseRepository {
    override suspend fun getById(id: ExerciseId): Exercise =
        withContext(ioDispatcher) {
            val dbExercise = exerciseDao.getById(id.toString())
            dbExercise.toExercise()
        }

    override fun queryExercises(query: ExerciseQuery): Flow<List<Exercise>> {
        val flow = if (query.categories.isEmpty()) {
            exerciseDao.getExercisesWithCategories(query.query)
        } else {
            exerciseDao
                .getExercisesWithCategories(
                    query = query.query,
                    categoriesNames = query.categories.map { it.name },
                )
        }
        return flow.map { list ->
            Timber.i("Exercises list size: ${list.size} for $query")
            list.map { it.toExercise() }
        }
    }

    override suspend fun insert(exercise: Exercise): Boolean =
        withContext(ioDispatcher) {
            val exerciseWithCategories = exercise.toDbExerciseWithCategories()
            exerciseDao.insertExerciseWithCategories(exerciseWithCategories)
        }

    override suspend fun delete(exercise: Exercise) =
        withContext(ioDispatcher) {
            exerciseDao.deleteExerciseById(exercise.id.toString()) == ONE_ROW
        }

    override suspend fun updateExercise(updatedExercise: Exercise): Boolean =
        withContext(ioDispatcher) {
            false
//            val dbExercise = updatedExercise.toDbExerciseWithCategories()
//            exerciseDao.update(dbExercise) == ONE_ROW
        }

    private companion object {
        const val ONE_ROW = 1
    }
}
