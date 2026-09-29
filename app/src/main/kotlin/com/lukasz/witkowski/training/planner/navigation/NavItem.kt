package com.lukasz.witkowski.training.planner.navigation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
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
data object ExercisesList : TrainingPlannerNavKey

@Serializable
data object CreateExercise : TrainingPlannerNavKey

@Serializable
data class ExerciseDetails(
    @Serializable(with = ExerciseIdSerializer::class)
    val exerciseId: ExerciseId,
    val exerciseName: String,
) : TrainingPlannerNavKey

@Serializable
data class EditExercise(
    @Serializable(with = ExerciseIdSerializer::class)
    val exerciseId: ExerciseId
) : TrainingPlannerNavKey

@Serializable
data object PickExercise : TrainingPlannerNavKey

@Serializable
data object TrainingPlansList : TrainingPlannerNavKey

@Serializable
data object CreateTraining : TrainingPlannerNavKey

@Serializable
data class TrainingOverview(
    @Serializable(with = TrainingPlanIdSerializer::class)
    val trainingPlanId: TrainingPlanId
) : TrainingPlannerNavKey

@Serializable
data class TrainingSession(
    @Serializable(with = TrainingPlanIdSerializer::class)
    val trainingPlanId: TrainingPlanId
) : TrainingPlannerNavKey {}


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

data class TopBarConfig(
    val title: String,
    val hasBackArrow: Boolean,
    val menuItems: List<TopBarMenuItem> = emptyList()
)

sealed interface TopBarAction {
    data class EditExercise(val id: ExerciseId) : TopBarAction
    data class DeleteExercise(val id: ExerciseId) : TopBarAction
}

sealed interface TopBarMenuItem {
    val action : TopBarAction
    data class IconItem(
        val icon: ImageVector,
        override val action: TopBarAction
    ): TopBarMenuItem
    data class OverflowItem(
        val icon: ImageVector,
        val text: String,
        override val action: TopBarAction,
        val color: Color? = null,
    ): TopBarMenuItem
}

fun TrainingPlannerNavKey.toTopBarConfig(context: Context): TopBarConfig =
    when (this) {
        is ExercisesList -> TopBarConfig(
            title = context.getString(R.string.exercises),
            hasBackArrow = false
        )

        is CreateExercise -> TopBarConfig(
            title = context.getString(R.string.create_exercise),
            hasBackArrow = true
        )

        is ExerciseDetails -> TopBarConfig(
            title = this.exerciseName,
            hasBackArrow = true,
            menuItems = listOf<TopBarMenuItem>(
                TopBarMenuItem.OverflowItem(
                    icon = Icons.Default.Edit,
                    text = context.getString(R.string.edit),
                    action = TopBarAction.EditExercise(this.exerciseId)
                ),
                TopBarMenuItem.OverflowItem(
                    icon = Icons.Default.Delete,
                    text = context.getString(R.string.delete),
                    action = TopBarAction.DeleteExercise(this.exerciseId),
                    color = Color.Red
                ),
            )
        )

        is EditExercise -> TopBarConfig(
            title = context.getString(R.string.edit_exercise),
            hasBackArrow = true
        )

        else -> TopBarConfig(title = "TODO", hasBackArrow = false)
    }


