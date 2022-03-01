package com.lukasz.witkowski.training.planner.exercise.domain

class ExerciseHandler(
    private val exerciseRepository: ExerciseRepository
) {

    fun createExercise(exercise: Exercise) {
        exerciseRepository.insert(exercise)
    }

    fun getAllExercises() : List<Exercise> {
        return exerciseRepository.getAll()
    }

    fun deleteExercise(exercise: Exercise) {
        exerciseRepository.delete(exercise)
    }

    fun getExerciseById(id: Long) : Exercise {
        return exerciseRepository.getById(id)
    }
}