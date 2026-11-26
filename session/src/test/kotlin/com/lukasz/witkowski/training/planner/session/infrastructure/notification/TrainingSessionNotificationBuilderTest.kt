package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.shared.utils.WeightUnit
import com.lukasz.witkowski.training.planner.shared.utils.kg
import com.lukasz.witkowski.training.planner.shared.utils.lbs
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TrainingSessionNotificationBuilderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val builder = TrainingSessionNotificationBuilder(context)

    @Test
    fun `builds Idle notification with no actions on ongoing channel`() {
        val notification = builder.build(TrainingNotificationUiState.Idle)

        assertEquals(TrainingNotificationChannelManager.CHANNEL_ID, notification.channelId)
        assertEquals(
            context.getString(R.string.notification_session_idle_title),
            notification.extras.getString(Notification.EXTRA_TITLE)
        )
        assertEquals(
            context.getString(R.string.notification_session_idle_text),
            notification.extras.getString(Notification.EXTRA_TEXT)
        )
        assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        assertEquals(0, notification.actions?.size ?: 0)
    }

    @Test
    fun `builds weighted rep-based ActiveExercise notification with Complete and Skip actions`() {
        val notification =
            builder.build(
                TrainingNotificationUiState.ActiveExercise(
                    exerciseName = "Bench Press",
                    currentSet = 2,
                    totalSets = 4,
                    reps = 10,
                    weight = 60.kg,
                    weightUnit = WeightUnit.KG,
                    holdSeconds = null,
                    timer = NotificationTimer.None,
                    progress =
                    NotificationProgress.WorkoutSegments(
                        completedSets = 3,
                        exerciseSetCounts = listOf(4, 3)
                    )
                )
            )

        assertEquals("Bench Press", notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals(
            "Set 2 of 4 • 10 reps • 60 kg",
            notification.extras.getString(Notification.EXTRA_TEXT)
        )
        assertEquals(3, notification.extras.getInt(Notification.EXTRA_PROGRESS))
        assertEquals(7, notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX))
        assertEquals(
            listOf(
                context.getString(R.string.notification_action_complete),
                context.getString(R.string.notification_action_skip)
            ),
            notification.actions.map { it.title.toString() }
        )
    }

    @Test
    fun `builds ActiveExercise notification in LBS when WeightUnit is LBS`() {
        val notification =
            builder.build(
                TrainingNotificationUiState.ActiveExercise(
                    exerciseName = "Deadlift",
                    currentSet = 1,
                    totalSets = 3,
                    reps = 5,
                    weight = 225.lbs,
                    weightUnit = WeightUnit.LBS,
                    holdSeconds = null,
                    timer = NotificationTimer.None,
                    progress =
                    NotificationProgress.WorkoutSegments(
                        completedSets = 0,
                        exerciseSetCounts = listOf(3)
                    )
                )
            )

        assertEquals(
            "Set 1 of 3 • 5 reps • 225 lbs",
            notification.extras.getString(Notification.EXTRA_TEXT)
        )
    }

    @Test
    fun `builds bodyweight rep-based ActiveExercise notification without weight`() {
        val notification =
            builder.build(
                TrainingNotificationUiState.ActiveExercise(
                    exerciseName = "Pull Up",
                    currentSet = 1,
                    totalSets = 3,
                    reps = 12,
                    weight = null,
                    holdSeconds = null,
                    timer = NotificationTimer.None,
                    progress =
                    NotificationProgress.WorkoutSegments(
                        completedSets = 0,
                        exerciseSetCounts = listOf(3)
                    )
                )
            )

        assertEquals(
            "Set 1 of 3 • 12 reps",
            notification.extras.getString(Notification.EXTRA_TEXT)
        )
    }

    @Test
    fun `builds isometric Ready ActiveExercise notification with Start timer action`() {
        val notification =
            builder.build(
                TrainingNotificationUiState.ActiveExercise(
                    exerciseName = "Weighted Plank",
                    currentSet = 1,
                    totalSets = 3,
                    reps = 1,
                    weight = 10.kg,
                    holdSeconds = 45L,
                    timer = NotificationTimer.Ready(totalSeconds = 45L),
                    progress =
                    NotificationProgress.WorkoutSegments(
                        completedSets = 0,
                        exerciseSetCounts = listOf(3)
                    )
                )
            )

        assertEquals("Weighted Plank", notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals(
            "Set 1 of 3 • 00:45 hold • 10 kg",
            notification.extras.getString(Notification.EXTRA_TEXT)
        )
        assertEquals(
            listOf(
                context.getString(R.string.notification_action_start_timer),
                context.getString(R.string.notification_action_complete),
                context.getString(R.string.notification_action_skip)
            ),
            notification.actions.map { it.title.toString() }
        )
    }

    @Test
    fun `builds isometric Running notification with timer in title and Reset action`() {
        val notification =
            builder.build(
                TrainingNotificationUiState.ActiveExercise(
                    exerciseName = "Plank",
                    currentSet = 2,
                    totalSets = 3,
                    reps = 1,
                    weight = null,
                    holdSeconds = 45L,
                    timer = NotificationTimer.Running(remainingSeconds = 30L),
                    progress =
                    NotificationProgress.WorkoutSegments(
                        completedSets = 1,
                        exerciseSetCounts = listOf(3)
                    )
                )
            )

        assertEquals("00:30 • Plank", notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals(
            "Set 2 of 3 • 00:45 hold",
            notification.extras.getString(Notification.EXTRA_TEXT)
        )
        assertEquals(
            listOf(
                context.getString(R.string.notification_action_reset_timer),
                context.getString(R.string.notification_action_complete),
                context.getString(R.string.notification_action_skip)
            ),
            notification.actions.map { it.title.toString() }
        )
    }

    @Test
    fun `builds Rest notification with timer in title, countdown progress, and Skip rest action`() {
        val notification =
            builder.build(
                TrainingNotificationUiState.Rest(
                    nextExerciseName = "Squat",
                    nextSet = 1,
                    nextTotalSets = 3,
                    timer = NotificationTimer.Running(remainingSeconds = 90L),
                    progress =
                    NotificationProgress.RestCountdown(
                        elapsedSeconds = 30,
                        totalSeconds = 120
                    )
                )
            )

        assertEquals("01:30 • Rest", notification.extras.getString(Notification.EXTRA_TITLE))
        assertEquals(
            "Up next: Squat (Set 1 of 3)",
            notification.extras.getString(Notification.EXTRA_TEXT)
        )
        assertEquals(30, notification.extras.getInt(Notification.EXTRA_PROGRESS))
        assertEquals(120, notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX))
        assertEquals(
            listOf(context.getString(R.string.notification_action_skip_rest)),
            notification.actions.map { it.title.toString() }
        )
    }

    @Test
    fun `builds Summary notification on alerts channel as dismissible autoCancel`() {
        val notification = builder.build(TrainingNotificationUiState.Summary())

        assertEquals(TrainingNotificationChannelManager.ALERTS_CHANNEL_ID, notification.channelId)
        assertEquals(
            context.getString(R.string.notification_summary_title),
            notification.extras.getString(Notification.EXTRA_TITLE)
        )
        assertEquals(
            context.getString(R.string.notification_summary_text),
            notification.extras.getString(Notification.EXTRA_TEXT)
        )
        assertEquals(100, notification.extras.getInt(Notification.EXTRA_PROGRESS))
        assertEquals(0, notification.actions?.size ?: 0)
        assertEquals(0, notification.flags and Notification.FLAG_ONGOING_EVENT)
        assertEquals(0, notification.flags and Notification.FLAG_ONLY_ALERT_ONCE)
        assertTrue(notification.flags and Notification.FLAG_AUTO_CANCEL != 0)
    }

    @Test
    fun `attaches contentIntent when SessionNotificationContentIntentProvider is provided`() {
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                99,
                Intent("test.action"),
                PendingIntent.FLAG_IMMUTABLE
            )
        val builderWithContentIntent =
            TrainingSessionNotificationBuilder(
                context = context,
                contentIntentProvider = SessionNotificationContentIntentProvider { pendingIntent }
            )

        val notification = builderWithContentIntent.build(TrainingNotificationUiState.Idle)

        assertEquals(pendingIntent, notification.contentIntent)
    }
}
