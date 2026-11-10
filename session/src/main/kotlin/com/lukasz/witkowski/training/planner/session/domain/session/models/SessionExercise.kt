package com.lukasz.witkowski.training.planner.session.domain.session.models

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration

data class SessionExercise(
    val snapshot: TrainingExerciseSnapshot,
    val currentConfig: SessionExerciseConfiguration,
    /**
     * Rest time after the Exercise
     */
    val restTime: Duration
) {
    val exerciseId = snapshot.id
}

data class TrainingExerciseSnapshot(
    val id: TrainingExerciseId,
    val exerciseId: ExerciseId,
    val name: String,
    val description: String,
    val categories: Set<ExerciseCategoryName>,
    val imageIds: List<ImageId> = emptyList(),
    val plannedConfig: SessionExerciseConfiguration
)
