package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.presentation.components.AppRpeScaleGrid
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.shared.time.formatToString
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.bento.AppBentoTile
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.inputField.AppLabeledTextField
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Workout session summary screen displaying performance metrics, RPE exertion selector,
 * session notes, exercise attempts breakdown, and save action.
 */
@Composable
fun TrainingSessionSummaryScreen(
    statistics: TrainingStatistics,
    trainingPlan: TrainingPlan,
    onSaveWorkout: (note: String?, rpe: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedRpe by remember { mutableStateOf<Int?>(statistics.rpe) }
    var notes by remember { mutableStateOf(statistics.note ?: "") }

    val allAttempts = statistics.exercisesStatistics.flatMap { it.attemptsStatistics }
    val totalVolumeKg = allAttempts.sumOf { (it.actualWeightInKg ?: 0.0) * it.actualReps }
    val totalReps = allAttempts.sumOf { it.actualReps }
    val completedSetsCount = allAttempts.count { it.completed }
    val totalSetsCount = allAttempts.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimens2.margin, vertical = Dimens2.spaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceLg)
    ) {
        // Celebration Banner
        CelebrationBanner(
            planTitle = trainingPlan.title,
            date = statistics.date
        )

        // Bento Metrics Grid
        BentoMetricsGrid(
            totalTime = statistics.totalTime,
            volumeKg = totalVolumeKg,
            completedSets = completedSetsCount,
            totalSets = totalSetsCount,
            totalReps = totalReps
        )

        // RPE Assessment Grid
        AppRpeScaleGrid(
            selectedRpe = selectedRpe,
            onRpeSelected = { selectedRpe = it }
        )

        // Workout Notes Input
        AppLabeledTextField(
            label = "WORKOUT NOTES",
            value = notes,
            onValueChange = { notes = it },
            placeholder = "Add observations, fatigue level, or progression goals...",
            singleLine = false
        )

        // Exercises Breakdown Section
        ExercisesBreakdownSection(
            statistics = statistics,
            trainingPlan = trainingPlan
        )

        // Save Button
        AppPrimaryButton(
            text = "Save & Finish Workout",
            leadingIcon = Icons.Default.Save,
            onClick = {
                onSaveWorkout(notes.ifBlank { null }, selectedRpe)
            }
        )
    }
}

@Composable
private fun CelebrationBanner(planTitle: String, date: Date, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        Box(
            modifier = Modifier
                .size(Dimens2.iconContainerSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Workout Complete",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(Dimens2.iconSize30)
            )
        }

        Text(
            text = "WORKOUT COMPLETE!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "$planTitle • ${date.formatToString()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BentoMetricsGrid(
    totalTime: Time,
    volumeKg: Double,
    completedSets: Int,
    totalSets: Int,
    totalReps: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            AppBentoTile(
                label = "Total Duration",
                value = formatDuration(totalTime),
                subtitle = "Active + rest",
                modifier = Modifier.weight(1f)
            )
            AppBentoTile(
                label = "Total Volume",
                value = if (volumeKg > 0) "${volumeKg.toInt()} kg" else "Bodyweight",
                subtitle = "Sum of all sets",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            AppBentoTile(
                label = "Sets Completed",
                value = "$completedSets / $totalSets",
                subtitle = "Total volume sets",
                modifier = Modifier.weight(1f)
            )
            AppBentoTile(
                label = "Total Repetitions",
                value = "$totalReps reps",
                subtitle = "Across all exercises",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ExercisesBreakdownSection(
    statistics: TrainingStatistics,
    trainingPlan: TrainingPlan,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        Text(
            text = "EXERCISE BREAKDOWN",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )

        statistics.exercisesStatistics.forEach { exerciseStats ->
            val exercise = trainingPlan.exercises.firstOrNull {
                it.id ==
                    exerciseStats.trainingExerciseId
            }
            val exerciseName = exercise?.exercise?.name ?: "Exercise"

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens2.spaceMd),
                    verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
                ) {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    exerciseStats.attemptsStatistics.forEachIndexed { index, attempt ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Set ${attempt.set}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val weight = attempt.actualWeightInKg
                            Text(
                                text = "${attempt.actualReps} reps" +
                                    if (weight != null && weight > 0.0) " • $weight kg" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val icon =
                                if (attempt.completed) Icons.Default.Check else Icons.Default.Close
                            val desc = if (attempt.completed) "Completed" else "Skipped"
                            val tint =
                                if (attempt.completed) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            Icon(
                                imageVector = icon,
                                contentDescription = desc,
                                tint = tint,
                                modifier = Modifier.size(Dimens2.iconSize16)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(time: Time): String {
    val totalSeconds = (time.timeInMillis / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

@AppPreview
@Composable
private fun TrainingSessionSummaryScreenPreview() {
    val exercise = TrainingExercise(
        id = TrainingExerciseId.create(),
        exercise = ExerciseSnapshot(
            id = com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId.create(),
            name = "Barbell Bench Press",
            description = "Chest press",
            categories = setOf(ExerciseCategoryName("Chest")),
            imageId = null
        ),
        repetitions = 10,
        sets = 3,
        restTime = 60.seconds
    )
    val plan = TrainingPlan(
        id = TrainingPlanId.create(),
        title = "Upper Body Hypertrophy",
        description = "Chest and triceps workout",
        exercises = listOf(exercise),
        restTime = 60.seconds,
        lastModification = Instant.DISTANT_PAST,
        lastSession = null
    )
    val stats = TrainingStatistics(
        trainingPlanId = plan.id,
        totalTime = Time(2700000L),
        date = Date(),
        exercisesStatistics = listOf(
            ExerciseStatistics(
                trainingExerciseId = exercise.id,
                attemptsStatistics = listOf(
                    ExerciseAttemptStatistics(
                        trainingExerciseId = exercise.id,
                        time = Time(45000L),
                        set = 1,
                        completed = true,
                        actualReps = 10,
                        actualWeightInKg = 85.0
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = exercise.id,
                        time = Time(45000L),
                        set = 2,
                        completed = true,
                        actualReps = 10,
                        actualWeightInKg = 85.0
                    ),
                    ExerciseAttemptStatistics(
                        trainingExerciseId = exercise.id,
                        time = Time(45000L),
                        set = 3,
                        completed = true,
                        actualReps = 9,
                        actualWeightInKg = 85.0
                    )
                )
            )
        )
    )

    TrainingPlannerTheme2 {
        TrainingSessionSummaryScreen(
            statistics = stats,
            trainingPlan = plan,
            onSaveWorkout = { _, _ -> }
        )
    }
}
