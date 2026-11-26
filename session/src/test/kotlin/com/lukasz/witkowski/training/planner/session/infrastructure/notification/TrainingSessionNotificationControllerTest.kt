package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import android.app.Notification
import android.app.NotificationManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertNotEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TrainingSessionNotificationControllerTest {
    private val notificationManager: NotificationManager = mockk(relaxed = true)
    private val channelManager: TrainingNotificationChannelManager = mockk(relaxed = true)
    private val notificationBuilder: TrainingSessionNotificationBuilder = mockk()
    private val controller =
        TrainingSessionNotificationController(
            notificationManager = notificationManager,
            channelManager = channelManager,
            notificationBuilder = notificationBuilder
        )

    @Test
    fun `update posts active notification and showCompleted posts to a separate ID`() {
        val activeNotification = Notification()
        val summaryNotification = Notification()
        val activeState = TrainingNotificationUiState.Idle
        val summaryState = TrainingNotificationUiState.Summary()
        every { notificationBuilder.build(activeState) } returns activeNotification
        every { notificationBuilder.build(summaryState) } returns summaryNotification

        var activeId = -1
        var completionId = -1
        every { notificationManager.notify(any(), activeNotification) } answers {
            activeId = firstArg()
        }
        every { notificationManager.notify(any(), summaryNotification) } answers {
            completionId = firstArg()
        }

        controller.update(activeState)
        controller.showCompleted(summaryState)

        verify(exactly = 1) {
            notificationManager.notify(controller.notificationId, activeNotification)
        }
        verify(exactly = 1) { notificationManager.notify(completionId, summaryNotification) }
        assertNotEquals(activeId, completionId)
    }

    @Test
    fun `cancelActive, cancelCompleted, and cancelAll cancel the expected notification IDs`() {
        val summaryNotification = Notification()
        val summaryState = TrainingNotificationUiState.Summary()
        every { notificationBuilder.build(summaryState) } returns summaryNotification

        var completionId = -1
        every { notificationManager.notify(any(), summaryNotification) } answers {
            completionId = firstArg()
        }
        controller.showCompleted(summaryState)

        controller.cancelActive()
        verify(exactly = 1) { notificationManager.cancel(controller.notificationId) }
        verify(exactly = 0) { notificationManager.cancel(completionId) }

        controller.cancelCompleted()
        verify(exactly = 1) { notificationManager.cancel(completionId) }

        controller.cancelAll()
        verify(exactly = 2) { notificationManager.cancel(controller.notificationId) }
        verify(exactly = 2) { notificationManager.cancel(completionId) }
    }
}
