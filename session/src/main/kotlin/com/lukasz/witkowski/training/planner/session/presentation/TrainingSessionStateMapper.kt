package com.lukasz.witkowski.training.planner.session.presentation
//
//import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
//import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
//import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
//import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState as DomainState
//import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
//import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
//import com.lukasz.witkowski.training.planner.shared.time.Time
//import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
//import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
//import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
//import kotlin.time.Duration
//import kotlin.time.Duration.Companion.milliseconds
//import kotlin.time.Instant
//
//fun DomainState.toPresentationTrainingSessionState(
//    trainingPlan: TrainingPlan? = null,
//    time: Time = Time.ZERO,
//    isTimerRunning: Boolean = false,
//    selectedReps: Int? = null,
//    selectedWeightInKg: Double? = null,
//    selectedHoldDuration: Duration? = null,
//    selectedFeedback: SetEffort? = null,
//    summaryRpe: SessionRpe? = null,
//    summaryNotes: String = ""
//): PresentationState = when (this) {
//    is DomainState.ExerciseState -> {
//        val totalSets = progress.currentExerciseTotalSets.coerceAtLeast(1)
//        val currentSet = (totalSets - progress.remainingExerciseSets).coerceAtLeast(1)
//        val totalExercises = progress.totalExercises.coerceAtLeast(1)
//        val exerciseIndex =
//            (totalExercises - progress.remainingExercises).coerceIn(1, totalExercises)
//        val targetHold = sessionExercise.snapshot.plannedConfig.holdTime ?: Duration.ZERO
//        val currentHold = sessionExercise.currentConfig.holdTime ?: targetHold
//        val remainingHold =
//            when (val ts = timerState) {
//                is TimerState.Running -> ts.remaining
//                is TimerState.Ready -> ts.duration
//                else -> if (currentHold >
//                    Duration.ZERO
//                ) {
//                    time.timeInMillis.milliseconds
//                } else {
//                    currentHold
//                }
//            }
//        val legacyExercise = sessionExercise.toTrainingExercise(totalSets)
//        PresentationState.ExerciseState(
//            currentExercise = legacyExercise,
//            sessionExercise = sessionExercise,
//            currentSet = currentSet,
//            totalSets = totalSets,
//            targetReps = sessionExercise.snapshot.plannedConfig.reps,
//            targetWeightInKg = sessionExercise.snapshot.plannedConfig.weightInKg,
//            targetTime = targetHold,
//            completedSets = emptyList(),
//            exerciseIndex = exerciseIndex,
//            totalExercises = totalExercises,
//            setsLeftInWorkout = progress.remainingSetsInWorkout,
//            followingExercise = null,
//            selectedReps = selectedReps ?: sessionExercise.currentConfig.reps,
//            selectedWeightInKg =
//            selectedWeightInKg ?: sessionExercise.currentConfig.weightInKg,
//            selectedHoldDuration = selectedHoldDuration ?: currentHold,
//            remainingHoldTime = remainingHold,
//            isHoldTimerRunning = isTimerRunning
//        )
//    }
//
//    is DomainState.RestTimeState -> {
//        val totalSets = progress.currentExerciseTotalSets.coerceAtLeast(1)
//        val nextSet = (totalSets - progress.remainingExerciseSets + 1).coerceIn(1, totalSets)
//        val legacyExercise = nextExercise.toTrainingExercise(totalSets)
//        PresentationState.RestTimeState(
//            nextExercise = legacyExercise,
//            restTime = Time(totalRestTime.inWholeMilliseconds),
//            nextSet = nextSet,
//            totalSetsForNextExercise = totalSets,
//            lastCompletedAttempt = null,
//            followingExercise = null,
//            selectedFeedback = selectedFeedback ?: this.selectedFeedback
//        )
//    }
//
//    is DomainState.SummaryState ->
//        PresentationState.SummaryState(
//            statistics = statistics,
//            trainingPlan =
//            trainingPlan ?: TrainingPlan(
//                id = statistics.trainingPlanId,
//                title = "",
//                description = "",
//                exercises = emptyList(),
//                restTime = Duration.ZERO,
//                lastModification = Instant.DISTANT_PAST,
//                lastSession = null
//            ),
//            selectedOverallRpe = summaryRpe ?: selectedOverallRpe,
//            notes = summaryNotes.ifEmpty { notes }
//        )
//
//    is DomainState.IdleState -> PresentationState.IdleState
//}
//
//private fun SessionExercise.toTrainingExercise(sets: Int): TrainingExercise = TrainingExercise(
//    id = snapshot.id,
//    exercise =
//    ExerciseSnapshot(
//        id = ExerciseId(snapshot.id.value),
//        name = snapshot.name,
//        description = snapshot.description,
//        categories = snapshot.categories,
//        imageIds = snapshot.imageIds
//    ),
//    repetitions = snapshot.plannedConfig.reps,
//    sets = sets,
//    restTime = restTime,
//    time = snapshot.plannedConfig.holdTime ?: Duration.ZERO,
//    weightInKg = snapshot.plannedConfig.weightInKg?.toInt()
//)
