package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.infrastructure.TrainingSessionForegroundService
import com.lukasz.witkowski.training.planner.session.presentation.formatRemainingTime
import com.lukasz.witkowski.training.planner.shared.R as SharedR
import com.lukasz.witkowski.training.planner.shared.utils.formatUpToTwoDecimals
import kotlin.time.Duration.Companion.seconds

class TrainingSessionNotificationBuilder(private val context: Context) {
    fun build(uiState: TrainingNotificationUiState): Notification {
        val content = mapToContent(uiState)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            buildPlatformNotification(content)
        } else {
            buildCompatNotification(content)
        }
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun buildPlatformNotification(content: NotificationContent): Notification {
        val builder =
            Notification.Builder(context, TrainingNotificationChannelManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.exercise_default)
                .setContentTitle(content.title)
                .setContentText(content.text)
                .setOngoing(content.ongoing)
                .setOnlyAlertOnce(true)

        content.subText?.let(builder::setSubText)
        content.shortCriticalText?.let(builder::setShortCriticalText)
        content.progress?.let { progress ->
            buildProgressStyle(progress)?.let(builder::setStyle)
        }
        content.actions.forEach { actionItem ->
            builder.addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, actionItem.iconRes),
                    actionItem.label,
                    createActionPendingIntent(actionItem.action)
                ).build()
            )
        }
        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun buildProgressStyle(progress: NotificationProgress): Notification.ProgressStyle? =
        when (progress) {
            is NotificationProgress.WorkoutSegments -> {
                if (progress.exerciseSetCounts.isEmpty()) {
                    null
                } else {
                    val totalSets = progress.totalSets
                    val segments =
                        progress.exerciseSetCounts.mapIndexed { index, setCount ->
                            Notification.ProgressStyle.Segment(setCount)
                                .setColor(
                                    WORKOUT_SEGMENT_COLORS[index % WORKOUT_SEGMENT_COLORS.size]
                                )
                        }
                    val points = buildExerciseBoundaryPoints(progress.exerciseSetCounts, totalSets)
                    Notification.ProgressStyle()
                        .setProgress(progress.completedSets.coerceIn(0, totalSets))
                        .setProgressSegments(segments)
                        .setProgressPoints(points)
                }
            }

            is NotificationProgress.RestCountdown -> {
                val total = progress.totalSeconds.coerceAtLeast(1)
                val segment =
                    Notification.ProgressStyle.Segment(total)
                        .setColor(REST_SEGMENT_COLOR)
                Notification.ProgressStyle()
                    .setProgress(progress.elapsedSeconds.coerceIn(0, total))
                    .setProgressSegments(listOf(segment))
            }

            is NotificationProgress.Completed -> {
                val segment =
                    Notification.ProgressStyle.Segment(1)
                        .setColor(COMPLETED_SEGMENT_COLOR)
                Notification.ProgressStyle()
                    .setProgress(1)
                    .setProgressSegments(listOf(segment))
            }
        }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun buildExerciseBoundaryPoints(
        exerciseSetCounts: List<Int>,
        totalSets: Int
    ): List<Notification.ProgressStyle.Point> {
        var cumulative = 0
        return buildList {
            exerciseSetCounts.dropLast(1).forEach { setCount ->
                cumulative += setCount
                if (cumulative in 1 until totalSets) {
                    add(
                        Notification.ProgressStyle.Point(cumulative)
                            .setColor(MILESTONE_POINT_COLOR)
                    )
                }
            }
        }
    }

    private fun buildCompatNotification(content: NotificationContent): Notification {
        val builder =
            NotificationCompat.Builder(context, TrainingNotificationChannelManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.exercise_default)
                .setContentTitle(content.title)
                .setContentText(content.text)
                .setOngoing(content.ongoing)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)

        content.subText?.let(builder::setSubText)
        when (val progress = content.progress) {
            is NotificationProgress.WorkoutSegments -> {
                if (progress.totalSets > 0) {
                    builder.setProgress(
                        progress.totalSets,
                        progress.completedSets.coerceIn(0, progress.totalSets),
                        false
                    )
                }
            }
            is NotificationProgress.RestCountdown -> {
                val total = progress.totalSeconds.coerceAtLeast(1)
                builder.setProgress(
                    total,
                    progress.elapsedSeconds.coerceIn(0, total),
                    false
                )
            }
            is NotificationProgress.Completed -> builder.setProgress(100, 100, false)
            null -> Unit
        }
        content.actions.forEach { actionItem ->
            builder.addAction(
                actionItem.iconRes,
                actionItem.label,
                createActionPendingIntent(actionItem.action)
            )
        }
        return builder.build()
    }

    private fun mapToContent(uiState: TrainingNotificationUiState): NotificationContent =
        when (uiState) {
            is TrainingNotificationUiState.Idle ->
                NotificationContent(
                    title = context.getString(R.string.notification_session_idle_title),
                    text = context.getString(R.string.notification_session_idle_text),
                    subText = context.getString(R.string.notification_subtext_active_session),
                    shortCriticalText = null,
                    progress = null,
                    actions = emptyList(),
                    ongoing = true
                )

            is TrainingNotificationUiState.ActiveExercise ->
                NotificationContent(
                    title = formatActiveExerciseTitle(uiState),
                    text = formatActiveExerciseText(uiState),
                    subText = context.getString(R.string.notification_subtext_active_session),
                    shortCriticalText = formatCriticalTimer(uiState.timer),
                    progress = uiState.progress,
                    actions = buildActiveExerciseActions(uiState.timer),
                    ongoing = true
                )

            is TrainingNotificationUiState.Rest ->
                NotificationContent(
                    title = formatRestTitle(uiState.timer),
                    text = formatRestText(uiState),
                    subText = context.getString(R.string.notification_subtext_rest),
                    shortCriticalText = formatCriticalTimer(uiState.timer),
                    progress = uiState.progress,
                    actions =
                    listOf(
                        ActionItem(
                            action = TrainingNotificationAction.SKIP,
                            iconRes = R.drawable.ic_skip,
                            label = context.getString(R.string.notification_action_skip_rest)
                        )
                    ),
                    ongoing = true
                )

            is TrainingNotificationUiState.Summary ->
                NotificationContent(
                    title = context.getString(R.string.notification_summary_title),
                    text = context.getString(R.string.notification_summary_text),
                    subText = null,
                    shortCriticalText = null,
                    progress = uiState.progress,
                    actions = emptyList(),
                    ongoing = true
                )
        }

    private fun formatActiveExerciseTitle(
        uiState: TrainingNotificationUiState.ActiveExercise
    ): String {
        val activeTimer =
            when (uiState.timer) {
                is NotificationTimer.Running,
                is NotificationTimer.Overflow,
                is NotificationTimer.Finished -> formatTimer(uiState.timer)
                is NotificationTimer.Ready,
                is NotificationTimer.None -> null
            }
        return if (activeTimer != null) {
            context.getString(
                R.string.notification_exercise_title_with_timer,
                activeTimer,
                uiState.exerciseName
            )
        } else {
            uiState.exerciseName
        }
    }

    private fun formatActiveExerciseText(
        uiState: TrainingNotificationUiState.ActiveExercise
    ): String {
        val parts =
            buildList {
                if (uiState.totalSets > 0) {
                    add(
                        context.getString(
                            R.string.session_set_progress_format,
                            uiState.currentSet,
                            uiState.totalSets
                        )
                    )
                }
                val holdSeconds = uiState.holdSeconds
                if (holdSeconds != null && holdSeconds > 0L) {
                    add(
                        context.getString(
                            R.string.session_target_hold_format,
                            formatRemainingTime(holdSeconds.seconds)
                        )
                    )
                } else if (uiState.reps > 0) {
                    add(
                        context.getString(
                            R.string.session_target_reps_format,
                            uiState.reps
                        )
                    )
                }
                val weightInKg = uiState.weightInKg
                if (weightInKg != null && weightInKg > 0.0) {
                    add(
                        context.getString(
                            SharedR.string.weight_kg_format,
                            weightInKg.formatUpToTwoDecimals()
                        )
                    )
                }
            }
        return parts.joinToString(separator = SEGMENT_SEPARATOR)
    }

    private fun buildActiveExerciseActions(timer: NotificationTimer): List<ActionItem> {
        val timerAction =
            when (timer) {
                is NotificationTimer.Ready ->
                    ActionItem(
                        action = TrainingNotificationAction.START_TIMER,
                        iconRes = R.drawable.ic_play,
                        label = context.getString(R.string.notification_action_start_timer)
                    )
                is NotificationTimer.Running,
                is NotificationTimer.Overflow,
                is NotificationTimer.Finished ->
                    ActionItem(
                        action = TrainingNotificationAction.RESET_TIMER,
                        iconRes = R.drawable.ic_reset,
                        label = context.getString(R.string.notification_action_reset_timer)
                    )
                is NotificationTimer.None -> null
            }
        return buildList {
            timerAction?.let(::add)
            add(
                ActionItem(
                    action = TrainingNotificationAction.COMPLETE,
                    iconRes = R.drawable.ic_complete,
                    label = context.getString(R.string.notification_action_complete)
                )
            )
            add(
                ActionItem(
                    action = TrainingNotificationAction.SKIP,
                    iconRes = R.drawable.ic_skip,
                    label = context.getString(R.string.notification_action_skip)
                )
            )
        }
    }

    private fun formatRestTitle(timer: NotificationTimer): String {
        val formattedTimer = formatCriticalTimer(timer)
        return if (formattedTimer != null) {
            context.getString(R.string.notification_rest_title_with_timer, formattedTimer)
        } else {
            context.getString(R.string.notification_rest_title)
        }
    }

    private fun formatRestText(uiState: TrainingNotificationUiState.Rest): String {
        val nextSet = uiState.nextSet
        val nextTotalSets = uiState.nextTotalSets
        return if (nextSet != null && nextTotalSets != null && nextTotalSets > 0) {
            val setLabel =
                context.getString(
                    R.string.session_set_progress_format,
                    nextSet,
                    nextTotalSets
                )
            context.getString(
                R.string.notification_rest_up_next_with_set,
                uiState.nextExerciseName,
                setLabel
            )
        } else {
            context.getString(
                R.string.notification_rest_up_next,
                uiState.nextExerciseName
            )
        }
    }

    private fun formatCriticalTimer(timer: NotificationTimer): String? = when (timer) {
        is NotificationTimer.Running,
        is NotificationTimer.Overflow -> formatTimer(timer)
        is NotificationTimer.Ready,
        is NotificationTimer.Finished,
        is NotificationTimer.None -> null
    }

    private fun formatTimer(timer: NotificationTimer): String? = when (timer) {
        is NotificationTimer.None -> null
        is NotificationTimer.Ready -> formatRemainingTime(timer.totalSeconds.seconds)
        is NotificationTimer.Running -> formatRemainingTime(timer.remainingSeconds.seconds)
        is NotificationTimer.Overflow ->
            "-${formatRemainingTime(timer.overflowSeconds.seconds)}"
        is NotificationTimer.Finished -> formatRemainingTime(0.seconds)
    }

    private fun createActionPendingIntent(action: TrainingNotificationAction): PendingIntent {
        val intent =
            Intent(context, TrainingSessionForegroundService::class.java).apply {
                this.action = action.intentAction
            }
        return PendingIntent.getService(
            context,
            action.requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private data class NotificationContent(
        val title: String,
        val text: String,
        val subText: String?,
        val shortCriticalText: String?,
        val progress: NotificationProgress?,
        val actions: List<ActionItem>,
        val ongoing: Boolean
    )

    private data class ActionItem(
        val action: TrainingNotificationAction,
        @DrawableRes val iconRes: Int,
        val label: String
    )

    private companion object {
        const val SEGMENT_SEPARATOR = " • "
        val WORKOUT_SEGMENT_COLORS =
            intArrayOf(
                Color.rgb(69, 220, 21),
                Color.rgb(255, 167, 38),
                Color.rgb(66, 165, 245)
            )
        val REST_SEGMENT_COLOR = Color.rgb(38, 166, 154)
        val COMPLETED_SEGMENT_COLOR = Color.rgb(69, 220, 21)
        val MILESTONE_POINT_COLOR = Color.WHITE
    }
}
