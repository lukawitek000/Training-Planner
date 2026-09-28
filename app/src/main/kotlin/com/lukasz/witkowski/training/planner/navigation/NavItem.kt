package com.lukasz.witkowski.training.planner.navigation

import androidx.navigation3.runtime.NavKey
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
sealed interface TrainingPlannerNavKey : NavKey

@Serializable
data object ExercisesList : TrainingPlannerNavKey {
}

@Serializable
data object CreateExercise: TrainingPlannerNavKey

@Serializable
data class ExerciseDetails(
    @Serializable(with = ExerciseIdSerializer::class)
    val exerciseId: ExerciseId
): TrainingPlannerNavKey


@Serializable
data class EditExercise(
    @Serializable(with = ExerciseIdSerializer::class)
    val exerciseId: ExerciseId
): TrainingPlannerNavKey

@Serializable
data object PickExercise: TrainingPlannerNavKey

@Serializable
data object TrainingPlansList : TrainingPlannerNavKey

@Serializable
data object CreateTraining: TrainingPlannerNavKey

@Serializable
data class TrainingOverview(
    @Serializable(with = TrainingPlanIdSerializer::class)
    val trainingPlanId: TrainingPlanId
): TrainingPlannerNavKey

@Serializable
data class TrainingSession(
    @Serializable(with = TrainingPlanIdSerializer::class)
    val trainingPlanId: TrainingPlanId
): TrainingPlannerNavKey {}


val BottomNavItems = listOf(TrainingPlansList, ExercisesList)


object TrainingPlanIdSerializer : KSerializer<TrainingPlanId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TrainingPlanId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TrainingPlanId) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): TrainingPlanId {
        val rawId = decoder.decodeString()
        return TrainingPlanId(rawId)
    }
}

object ExerciseIdSerializer : KSerializer<ExerciseId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ExerciseId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ExerciseId) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): ExerciseId {
        val rawId = decoder.decodeString()
        return ExerciseId(rawId)
    }
}
