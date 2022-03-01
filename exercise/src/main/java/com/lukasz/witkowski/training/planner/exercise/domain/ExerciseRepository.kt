package com.lukasz.witkowski.training.planner.exercise.domain

interface ExerciseRepository {
    fun getById(id: Long) : Exercise
    fun getAll() : List<Exercise>
    fun insert(exercise: Exercise)
    fun delete(exercise: Exercise)
}