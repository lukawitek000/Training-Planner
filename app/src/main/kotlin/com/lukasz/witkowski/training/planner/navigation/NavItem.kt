package com.lukasz.witkowski.training.planner.navigation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.lukasz.witkowski.training.planner.R
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
sealed interface ExerciseIdRoute {
    val exerciseId: ExerciseId
}

@Serializable
data class ExerciseDetails(
    @Serializable(with = ExerciseIdSerializer::class)
    override val exerciseId: ExerciseId,
) : TrainingPlannerNavKey, ExerciseIdRoute


@Serializable
data class EditExercise(
    @Serializable(with = ExerciseIdSerializer::class)
    val exerciseId: ExerciseId
) : TrainingPlannerNavKey

@Serializable
data class DeleteExercise(
    @Serializable(with = ExerciseIdSerializer::class)
    override val exerciseId: ExerciseId,
) : TrainingPlannerNavKey, ExerciseIdRoute

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

data class FabConfig(
    val icon: ImageVector,
    val contentDescription: String? = null,
    val onClick: () -> Unit,
)

data class BottomBarConfig(
    val items: List<BottomBarItem>,
)

data class UiConfig(
    val topBarConfig: TopBarConfig,
    val bottomBarConfig: BottomBarConfig? = null,
    val fabConfig: FabConfig? = null,
)

private enum class BottomNavigationTab {
    TRAINING_PLANS,
    EXERCISES,
}

private fun TrainingPlannerNavigator.createBottomBarConfig(
    context: Context,
    selectedTab: BottomNavigationTab,
): BottomBarConfig {
    return BottomBarConfig(
        items = listOf(
            BottomBarItem(
                icon = R.drawable.trainings_icon,
                title = "Training Plans",
                selected = selectedTab == BottomNavigationTab.TRAINING_PLANS,
                onClick = { trainingPlansList() }
            ),
            BottomBarItem(
                icon = R.drawable.exercises_icon,
                title = context.getString(R.string.exercises),
                selected = selectedTab == BottomNavigationTab.EXERCISES,
                onClick = { exerciseList() }
            )
        )
    )
}

fun TrainingPlannerNavigator.toUiConfig(context: Context): UiConfig =
    when (val key = current()) {
        is TrainingPlansList -> UiConfig(
            topBarConfig = TopBarConfig(
                title = context.getString(R.string.app_name),
                hasBackArrow = false
            ),
            bottomBarConfig = createBottomBarConfig(context, BottomNavigationTab.TRAINING_PLANS),
            fabConfig = FabConfig(
                icon = Icons.Default.Add,
                contentDescription = context.getString(R.string.create_training),
                onClick = { trainingCreate() }
            )
        )

        is ExercisesList -> UiConfig(
            topBarConfig = TopBarConfig(
                title = context.getString(R.string.exercises),
                hasBackArrow = false
            ),
            bottomBarConfig = createBottomBarConfig(context, BottomNavigationTab.EXERCISES),
            fabConfig = FabConfig(
                icon = Icons.Default.Add,
                contentDescription = context.getString(R.string.create_exercise),
                onClick = { exerciseCreate() }
            )
        )

        is CreateExercise -> UiConfig(
            topBarConfig = TopBarConfig(
                title = context.getString(R.string.create_exercise),
                hasBackArrow = true
            )
        )

        is EditExercise -> UiConfig(
            topBarConfig = TopBarConfig(
                title = context.getString(R.string.edit_exercise),
                hasBackArrow = true
            )
        )

        is DeleteExercise, is ExerciseDetails -> UiConfig(
            topBarConfig = TopBarConfig(
                title = context.getString(R.string.exercise_details),
                hasBackArrow = true,
                menuItems = listOf(
                    TopBarMenuItem.OverflowItem(
                        icon = Icons.Default.Edit,
                        text = context.getString(R.string.edit),
                        action = TopBarAction.EditExercise(key.exerciseId)
                    ),
                    TopBarMenuItem.OverflowItem(
                        icon = Icons.Default.Delete,
                        text = context.getString(R.string.delete),
                        action = TopBarAction.DeleteExercise(key.exerciseId),
                        color = Color.Red
                    ),
                )
            )
        )



        is CreateTraining -> UiConfig(
            topBarConfig = TopBarConfig(
                title = context.getString(R.string.create_training),
                hasBackArrow = true
            )
        )

        is PickExercise -> UiConfig(
            topBarConfig = TopBarConfig(
                title = context.getString(R.string.search_exercise),
                hasBackArrow = true
            )
        )

        is TrainingOverview -> UiConfig(
            topBarConfig = TopBarConfig(
                title = context.getString(R.string.training_statistics),
                hasBackArrow = true
            )
        )

        is TrainingSession -> UiConfig(
            topBarConfig = TopBarConfig(
                title = context.getString(R.string.start_training_session),
                hasBackArrow = true
            )
        )

        else -> UiConfig(
            topBarConfig = TopBarConfig(title = "TODO", hasBackArrow = false)
        )
    }


