package com.lukasz.witkowski.training.planner.training

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanOverview
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object TestData {
    // --- IDs ---
    val TRAINING_PLAN_ID_1 = TrainingPlanId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
    val TRAINING_PLAN_ID_2 = TrainingPlanId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
    val TRAINING_PLAN_ID_3 = TrainingPlanId(UUID.fromString("33333333-3333-3333-3333-333333333333"))

    val TRAINING_EXERCISE_ID_1 = TrainingExerciseId(UUID.fromString("44444444-4444-4444-4444-444444444444"))
    val TRAINING_EXERCISE_ID_2 = TrainingExerciseId(UUID.fromString("55555555-5555-5555-5555-555555555555"))
    val TRAINING_EXERCISE_ID_3 = TrainingExerciseId(UUID.fromString("66666666-6666-6666-6666-666666666666"))
    val TRAINING_EXERCISE_ID_4 = TrainingExerciseId(UUID.fromString("77777777-7777-7777-7777-777777777777"))

    val EXERCISE_ID_1 = ExerciseId(UUID.fromString("88888888-8888-8888-8888-888888888888"))
    val EXERCISE_ID_2 = ExerciseId(UUID.fromString("99999999-9999-9999-9999-999999999999"))
    val EXERCISE_ID_3 = ExerciseId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
    val EXERCISE_ID_4 = ExerciseId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))

    val IMAGE_ID_1 = ImageId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"))

    // --- Categories ---
    val CATEGORY_CHEST = ExerciseCategoryName("Chest")
    val CATEGORY_BACK = ExerciseCategoryName("Back")
    val CATEGORY_LEGS = ExerciseCategoryName("Legs")
    val CATEGORY_ARMS = ExerciseCategoryName("Arms")
    val CATEGORY_ABS = ExerciseCategoryName("Abs")
    val CATEGORY_CARDIO = ExerciseCategoryName("Cardio")
    val CATEGORY_SHOULDERS = ExerciseCategoryName("Shoulders")

    // --- Exercise Snapshots ---
    val PUSH_UPS_SNAPSHOT =
        ExerciseSnapshot(
            id = EXERCISE_ID_1,
            name = "Push-ups",
            description = "Chest and arms bodyweight exercise",
            categories = setOf(CATEGORY_CHEST, CATEGORY_ARMS),
            imageId = IMAGE_ID_1,
        )

    val SQUATS_SNAPSHOT =
        ExerciseSnapshot(
            id = EXERCISE_ID_2,
            name = "Squats",
            description = "Lower body legs exercise",
            categories = setOf(CATEGORY_LEGS),
        )

    val PULL_UPS_SNAPSHOT =
        ExerciseSnapshot(
            id = EXERCISE_ID_3,
            name = "Pull-ups",
            description = "Back and arms upper body exercise",
            categories = setOf(CATEGORY_BACK, CATEGORY_ARMS),
        )

    val PLANK_SNAPSHOT =
        ExerciseSnapshot(
            id = EXERCISE_ID_4,
            name = "Plank",
            description = "Core stability exercise",
            categories = setOf(CATEGORY_ABS),
        )

    val RUNNING_SNAPSHOT =
        ExerciseSnapshot(
            id = ExerciseId.create(),
            name = "Running",
            description = "Endurance cardio exercise",
            categories = setOf(CATEGORY_CARDIO),
        )

    val EXERCISE_SNAPSHOTS_LIST =
        listOf(
            PUSH_UPS_SNAPSHOT,
            SQUATS_SNAPSHOT,
            PULL_UPS_SNAPSHOT,
            PLANK_SNAPSHOT,
            RUNNING_SNAPSHOT,
        )

    // --- Training Exercises ---
    val TRAINING_EXERCISE_PUSH_UPS =
        TrainingExercise(
            id = TRAINING_EXERCISE_ID_1,
            exercise = PUSH_UPS_SNAPSHOT,
            repetitions = 12,
            sets = 3,
            restTime = 60.seconds,
            weightInKg = null,
        )

    val TRAINING_EXERCISE_SQUATS =
        TrainingExercise(
            id = TRAINING_EXERCISE_ID_2,
            exercise = SQUATS_SNAPSHOT,
            repetitions = 15,
            sets = 4,
            restTime = 90.seconds,
            weightInKg = 20,
        )

    val TRAINING_EXERCISE_PULL_UPS =
        TrainingExercise(
            id = TRAINING_EXERCISE_ID_3,
            exercise = PULL_UPS_SNAPSHOT,
            repetitions = 8,
            sets = 3,
            restTime = 60.seconds,
            weightInKg = null,
        )

    val TRAINING_EXERCISE_PLANK =
        TrainingExercise(
            id = TRAINING_EXERCISE_ID_4,
            exercise = PLANK_SNAPSHOT,
            repetitions = 1,
            sets = 3,
            restTime = 45.seconds,
            weightInKg = null,
        )

    // --- Training Plans (Different kinds, multiple exercises, different categories) ---
    val FULL_BODY_TRAINING_PLAN =
        TrainingPlan(
            id = TRAINING_PLAN_ID_1,
            title = "Full Body Workout",
            description = "A comprehensive full body routine covering chest, legs, back, and core.",
            exercises = listOf(
                PUSH_UPS_SNAPSHOT,
                SQUATS_SNAPSHOT,
                PULL_UPS_SNAPSHOT,
                PLANK_SNAPSHOT,
            ),
            restTime = 60.seconds,
        )

    val CARDIO_ENDURANCE_TRAINING_PLAN =
        TrainingPlan(
            id = TRAINING_PLAN_ID_2,
            title = "Cardio Endurance",
            description = "High energy cardio and stamina workout.",
            exercises = listOf(
                RUNNING_SNAPSHOT,
            ),
            restTime = 30.seconds,
        )

    val UPPER_BODY_STRENGTH_TRAINING_PLAN =
        TrainingPlan(
            id = TRAINING_PLAN_ID_3,
            title = "Upper Body Strength",
            description = "Focused upper body training emphasizing chest, back, and arms.",
            exercises = listOf(
                PUSH_UPS_SNAPSHOT,
                PULL_UPS_SNAPSHOT,
            ),
            restTime = 90.seconds,
        )

    val TRAINING_PLANS_LIST =
        listOf(
            FULL_BODY_TRAINING_PLAN,
            CARDIO_ENDURANCE_TRAINING_PLAN,
            UPPER_BODY_STRENGTH_TRAINING_PLAN,
        )

    // --- Training Plan Overviews ---
    val FULL_BODY_TRAINING_PLAN_OVERVIEW =
        TrainingPlanOverview(
            id = FULL_BODY_TRAINING_PLAN.id,
            title = FULL_BODY_TRAINING_PLAN.title,
            description = FULL_BODY_TRAINING_PLAN.description,
            categories = FULL_BODY_TRAINING_PLAN.exercises.flatMap { it.categories }.toSet(),
        )

    val CARDIO_ENDURANCE_TRAINING_PLAN_OVERVIEW =
        TrainingPlanOverview(
            id = CARDIO_ENDURANCE_TRAINING_PLAN.id,
            title = CARDIO_ENDURANCE_TRAINING_PLAN.title,
            description = CARDIO_ENDURANCE_TRAINING_PLAN.description,
            categories = CARDIO_ENDURANCE_TRAINING_PLAN.exercises.flatMap { it.categories }.toSet(),
        )

    val UPPER_BODY_STRENGTH_TRAINING_PLAN_OVERVIEW =
        TrainingPlanOverview(
            id = UPPER_BODY_STRENGTH_TRAINING_PLAN.id,
            title = UPPER_BODY_STRENGTH_TRAINING_PLAN.title,
            description = UPPER_BODY_STRENGTH_TRAINING_PLAN.description,
            categories = UPPER_BODY_STRENGTH_TRAINING_PLAN.exercises.flatMap { it.categories }.toSet(),
        )

    val TRAINING_PLAN_OVERVIEWS_LIST =
        listOf(
            FULL_BODY_TRAINING_PLAN_OVERVIEW,
            CARDIO_ENDURANCE_TRAINING_PLAN_OVERVIEW,
            UPPER_BODY_STRENGTH_TRAINING_PLAN_OVERVIEW,
        )

    // --- Factory Helper Functions ---
    fun createTrainingPlan(
        id: TrainingPlanId = TrainingPlanId.create(),
        title: String = "Test Training Plan",
        description: String = "Test Description",
        exercises: List<ExerciseSnapshot> = listOf(PUSH_UPS_SNAPSHOT, SQUATS_SNAPSHOT),
        restTime: Duration = 60.seconds,
    ): TrainingPlan =
        TrainingPlan(
            id = id,
            title = title,
            description = description,
            exercises = exercises,
            restTime = restTime,
        )

    fun createExerciseSnapshot(
        id: ExerciseId = ExerciseId.create(),
        name: String = "Test Exercise",
        description: String = "Test Exercise Description",
        categories: Set<ExerciseCategoryName> = setOf(CATEGORY_CHEST),
        imageId: ImageId? = null,
    ): ExerciseSnapshot =
        ExerciseSnapshot(
            id = id,
            name = name,
            description = description,
            categories = categories,
            imageId = imageId,
        )

    fun createTrainingExercise(
        id: TrainingExerciseId = TrainingExerciseId.create(),
        exercise: ExerciseSnapshot = PUSH_UPS_SNAPSHOT,
        repetitions: Int = 10,
        sets: Int = 3,
        restTime: Duration = 30.seconds,
        weightInKg: Int? = null,
    ): TrainingExercise =
        TrainingExercise(
            id = id,
            exercise = exercise,
            repetitions = repetitions,
            sets = sets,
            restTime = restTime,
            weightInKg = weightInKg,
        )
}
