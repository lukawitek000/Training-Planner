package com.lukasz.witkowski.training.planner.exercise

import com.lukasz.witkowski.training.planner.exercise.application.ExerciseConfiguration
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryLegacy
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Recommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendationLevel
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import com.lukasz.witkowski.training.planner.image.ImageByteArray
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise2 as PresentationExercise2
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseDetails as PresentationExerciseDetails

object TestData {
    // --- IDs ---
    val EXERCISE_ID_1 = ExerciseId("11111111-1111-1111-1111-111111111111")
    val EXERCISE_ID_2 = ExerciseId("22222222-2222-2222-2222-222222222222")
    val EXERCISE_ID_3 = ExerciseId("33333333-3333-3333-3333-333333333333")
    val EXERCISE_ID_4 = ExerciseId("44444444-4444-4444-4444-444444444444")

    val IMAGE_ID_1 = ImageId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
    val IMAGE_ID_2 = ImageId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))

    val SAMPLE_IMAGE_BYTE_ARRAY = ImageByteArray(byteArrayOf(1, 2, 3, 4, 5))
    val SAMPLE_IMAGE_REFERENCE = ImageReference(IMAGE_ID_1, "path/to/image1.jpg")

    // --- Categories ---
    val CATEGORY_CHEST = ExerciseCategory("Chest")
    val CATEGORY_TRICEPS = ExerciseCategory("Triceps")
    val CATEGORY_LEGS = ExerciseCategory("Legs")
    val CATEGORY_BACK = ExerciseCategory("Back")
    val CATEGORY_BICEPS = ExerciseCategory("Biceps")
    val CATEGORY_ABS = ExerciseCategory("Abs")
    val CATEGORY_SHOULDERS = ExerciseCategory("Shoulders")
    val CATEGORY_CARDIO = ExerciseCategory("Cardio")

    val CATEGORIES_LIST =
        listOf(
            CATEGORY_CHEST,
            CATEGORY_TRICEPS,
            CATEGORY_LEGS,
            CATEGORY_BACK,
            CATEGORY_BICEPS,
            CATEGORY_ABS,
            CATEGORY_SHOULDERS,
            CATEGORY_CARDIO,
        )

    // --- Recommendations ---
    val BEGINNER_RECOMMENDATION =
        ExerciseRecommendation(
            sets = 3,
            reps = 10,
            restTime = 60.seconds,
            weightInKg = 0,
        )

    val INTERMEDIATE_RECOMMENDATION =
        ExerciseRecommendation(
            sets = 4,
            reps = 12,
            restTime = 45.seconds,
            weightInKg = 10,
        )

    val ADVANCED_RECOMMENDATION =
        ExerciseRecommendation(
            sets = 5,
            reps = 15,
            restTime = 30.seconds,
            weightInKg = 20,
        )

    // --- Domain Exercises (Exercise2) ---
    val PUSH_UPS_EXERCISE =
        createExercise2(
            id = EXERCISE_ID_1,
            name = "Push-up",
            description = "Standard push-up focusing on chest and triceps, BODYWEIGHT",
            categories = listOf(CATEGORY_CHEST, CATEGORY_TRICEPS, CATEGORY_BICEPS),
            imageId = IMAGE_ID_1,
        )

    val SQUATS_EXERCISE =
        createExercise2(
            id = EXERCISE_ID_2,
            name = "Bodyweight Squat",
            description = "Basic bodyweight squat targeting legs and glutes",
            categories = listOf(CATEGORY_LEGS),
        )

    val PULL_UPS_EXERCISE =
        createExercise2(
            id = EXERCISE_ID_3,
            name = "Pull-up",
            description = "Wide-grip pull-up for back and biceps",
            categories = listOf(CATEGORY_BACK, CATEGORY_BICEPS),
            imageId = IMAGE_ID_2,
        )

    val PLANK_EXERCISE =
        createExercise2(
            id = EXERCISE_ID_4,
            name = "Plank",
            description = "Core isometric hold",
            categories = listOf(CATEGORY_ABS),
        )

    val EXERCISES_LIST =
        listOf(
            PUSH_UPS_EXERCISE,
            SQUATS_EXERCISE,
            PULL_UPS_EXERCISE,
            PLANK_EXERCISE,
        )

    // --- Domain ExerciseDetails ---
    val PUSH_UPS_DETAILS =
        createExerciseDetails(
            exercise = PUSH_UPS_EXERCISE,
            beginnerRecommendation = BEGINNER_RECOMMENDATION,
            intermediateRecommendation = INTERMEDIATE_RECOMMENDATION,
            advancedRecommendation = ADVANCED_RECOMMENDATION,
        )

    val SQUATS_DETAILS =
        createExerciseDetails(
            exercise = SQUATS_EXERCISE,
            beginnerRecommendation = BEGINNER_RECOMMENDATION.copy(reps = 12),
            intermediateRecommendation = INTERMEDIATE_RECOMMENDATION.copy(reps = 15, weightInKg = 20),
            advancedRecommendation = ADVANCED_RECOMMENDATION.copy(reps = 20, weightInKg = 40),
        )

    val PULL_UPS_DETAILS =
        createExerciseDetails(
            exercise = PULL_UPS_EXERCISE,
            beginnerRecommendation = BEGINNER_RECOMMENDATION.copy(reps = 5),
            intermediateRecommendation = INTERMEDIATE_RECOMMENDATION.copy(reps = 8),
            advancedRecommendation = ADVANCED_RECOMMENDATION.copy(reps = 12, weightInKg = 10),
        )

    val PLANK_DETAILS =
        createExerciseDetails(
            exercise = PLANK_EXERCISE,
            beginnerRecommendation = ExerciseRecommendation(sets = 3, reps = 1, restTime = 30.seconds, weightInKg = null),
            intermediateRecommendation = ExerciseRecommendation(sets = 3, reps = 1, restTime = 45.seconds, weightInKg = null),
            advancedRecommendation = ExerciseRecommendation(sets = 4, reps = 1, restTime = 60.seconds, weightInKg = 10),
        )

    val EXERCISE_DETAILS_LIST =
        listOf(
            PUSH_UPS_DETAILS,
            SQUATS_DETAILS,
            PULL_UPS_DETAILS,
            PLANK_DETAILS,
        )

    // --- Legacy Domain Exercises ---
    val LEGACY_PUSH_UPS_EXERCISE =
        createLegacyExercise(
            id = EXERCISE_ID_1,
            name = "Push-up",
            description = "Standard push-up focusing on chest and triceps",
            categories = listOf(ExerciseCategoryLegacy.CHEST, ExerciseCategoryLegacy.TRICEPS),
            imageId = IMAGE_ID_1,
        )

    val LEGACY_SQUATS_EXERCISE =
        createLegacyExercise(
            id = EXERCISE_ID_2,
            name = "Bodyweight Squat",
            description = "Basic bodyweight squat targeting legs and glutes",
            categories = listOf(ExerciseCategoryLegacy.LEGS),
        )

    val LEGACY_EXERCISES_LIST =
        listOf(
            LEGACY_PUSH_UPS_EXERCISE,
            LEGACY_SQUATS_EXERCISE,
        )

    // --- Exercise Configurations (Application Layer) ---
    val PUSH_UPS_CONFIGURATION =
        createExerciseConfiguration(
            name = PUSH_UPS_EXERCISE.name,
            description = PUSH_UPS_EXERCISE.description,
            categories = PUSH_UPS_EXERCISE.categories.toList(),
            image = SAMPLE_IMAGE_BYTE_ARRAY,
            beginnerRecommendation = BEGINNER_RECOMMENDATION,
            intermediateRecommendation = INTERMEDIATE_RECOMMENDATION,
            advancedRecommendation = ADVANCED_RECOMMENDATION,
        )

    val SQUATS_CONFIGURATION =
        createExerciseConfiguration(
            name = SQUATS_EXERCISE.name,
            description = SQUATS_EXERCISE.description,
            categories = SQUATS_EXERCISE.categories.toList(),
            beginnerRecommendation = BEGINNER_RECOMMENDATION.copy(reps = 12),
            intermediateRecommendation = INTERMEDIATE_RECOMMENDATION.copy(reps = 15, weightInKg = 20),
            advancedRecommendation = ADVANCED_RECOMMENDATION.copy(reps = 20, weightInKg = 40),
        )

    // --- Exercise Queries ---
    val EMPTY_QUERY = ExerciseQuery(categories = emptyList(), query = "")
    val CHEST_QUERY = ExerciseQuery(categories = listOf(CATEGORY_CHEST), query = "")
    val SEARCH_PUSH_QUERY = ExerciseQuery(categories = emptyList(), query = "Push")
    val CHEST_PUSH_QUERY = ExerciseQuery(categories = listOf(CATEGORY_CHEST), query = "Push")

    // --- Presentation Models ---
    val PRESENTATION_PUSH_UPS_EXERCISE =
        PresentationExercise2(
            id = EXERCISE_ID_1,
            name = "Push-up",
            description = "Standard push-up focusing on chest and triceps",
            categories = listOf(CATEGORY_CHEST, CATEGORY_TRICEPS),
            image = SAMPLE_IMAGE_REFERENCE,
        )

    val PRESENTATION_PUSH_UPS_DETAILS =
        PresentationExerciseDetails(
            exercise = PRESENTATION_PUSH_UPS_EXERCISE,
            recommendations =
                listOf(
                    Recommendation(
                        level = RecommendationLevel.BEGINNER,
                        parameters =
                            RecommendedParameters(
                                sets = BEGINNER_RECOMMENDATION.sets,
                                reps = BEGINNER_RECOMMENDATION.reps,
                                restTime = BEGINNER_RECOMMENDATION.restTime,
                                weightInKg = BEGINNER_RECOMMENDATION.weightInKg,
                            ),
                    ),
                    Recommendation(
                        level = RecommendationLevel.INTERMEDIATE,
                        parameters =
                            RecommendedParameters(
                                sets = INTERMEDIATE_RECOMMENDATION.sets,
                                reps = INTERMEDIATE_RECOMMENDATION.reps,
                                restTime = INTERMEDIATE_RECOMMENDATION.restTime,
                                weightInKg = INTERMEDIATE_RECOMMENDATION.weightInKg,
                            ),
                    ),
                    Recommendation(
                        level = RecommendationLevel.ADVANCED,
                        parameters =
                            RecommendedParameters(
                                sets = ADVANCED_RECOMMENDATION.sets,
                                reps = ADVANCED_RECOMMENDATION.reps,
                                restTime = ADVANCED_RECOMMENDATION.restTime,
                                weightInKg = ADVANCED_RECOMMENDATION.weightInKg,
                            ),
                    ),
                ),
        )

    // --- Factory Helper Functions ---
    fun createExercise2(
        id: ExerciseId = ExerciseId.create(),
        name: String = "Test Exercise",
        description: String = "Test Description",
        categories: List<ExerciseCategory> = listOf(CATEGORY_CHEST),
        imageId: ImageId? = null,
    ): Exercise2 =
        Exercise2(
            id = id,
            name = name,
            description = description,
            categories = categories.toSet(),
            imageId = imageId,
        )

    fun createExerciseDetails(
        exercise: Exercise2 = createExercise2(),
        beginnerRecommendation: ExerciseRecommendation = BEGINNER_RECOMMENDATION,
        intermediateRecommendation: ExerciseRecommendation = INTERMEDIATE_RECOMMENDATION,
        advancedRecommendation: ExerciseRecommendation = ADVANCED_RECOMMENDATION,
    ): ExerciseDetails =
        ExerciseDetails(
            exercise = exercise,
            beginnerRecommendation = beginnerRecommendation,
            intermediateRecommendation = intermediateRecommendation,
            advancedRecommendation = advancedRecommendation,
        )

    fun createLegacyExercise(
        id: ExerciseId = ExerciseId.create(),
        name: String = "Legacy Test Exercise",
        description: String = "Legacy Test Description",
        categories: List<ExerciseCategoryLegacy> = listOf(ExerciseCategoryLegacy.CHEST),
        imageId: ImageId? = null,
    ): Exercise =
        Exercise(
            id = id,
            name = name,
            description = description,
            categories = categories,
            imageId = imageId,
        )

    fun createExerciseConfiguration(
        name: String = "Test Exercise Configuration",
        description: String = "Test Configuration Description",
        categories: List<ExerciseCategory> = listOf(CATEGORY_CHEST),
        image: ImageByteArray? = null,
        beginnerRecommendation: ExerciseRecommendation = BEGINNER_RECOMMENDATION,
        intermediateRecommendation: ExerciseRecommendation = INTERMEDIATE_RECOMMENDATION,
        advancedRecommendation: ExerciseRecommendation = ADVANCED_RECOMMENDATION,
    ): ExerciseConfiguration =
        ExerciseConfiguration(
            name = name,
            description = description,
            categories = categories,
            image = image,
            beginnerRecommendation = beginnerRecommendation,
            intermediateRecommendation = intermediateRecommendation,
            advancedRecommendation = advancedRecommendation,
        )
}
