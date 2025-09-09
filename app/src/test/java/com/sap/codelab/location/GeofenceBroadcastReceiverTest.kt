package com.sap.codelab.location

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.notification.NotificationUtils
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GeofenceBroadcastReceiverTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var receiver: GeofenceBroadcastReceiver
    private lateinit var context: Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        receiver = GeofenceBroadcastReceiver()

        mockkObject(Repository)
        mockkObject(NotificationUtils)

        every { NotificationUtils.createNotificationChannel(any()) } just Runs
        every { NotificationUtils.showMemoNotification(any(), any()) } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun test_onReceive_geofenceEnter_callsNotification() = runTest {
        val memoId = 123L
        val memo = Memo(
            id = memoId,
            title = "Test Title",
            description = "Test Description",
            reminderLatitude = 10.0,
            reminderLongitude = 20.0,
            reminderDate = 0L
        )

        coEvery { Repository.getMemoById(memoId) } returns memo

        val geofencingEvent = mockk<GeofencingEvent>(relaxed = true)
        every { geofencingEvent.hasError() } returns false
        every { geofencingEvent.geofenceTransition } returns Geofence.GEOFENCE_TRANSITION_ENTER
        every { geofencingEvent.triggeringGeofences } returns listOf(mockk {
            every { requestId } returns memoId.toString()
        })

        mockkStatic(GeofencingEvent::class)
        every { GeofencingEvent.fromIntent(any()) } returns geofencingEvent

        val intent = Intent()

        receiver.onReceive(context, intent)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { Repository.getMemoById(memoId) }
        verify { NotificationUtils.createNotificationChannel(context) }
        verify { NotificationUtils.showMemoNotification(context, memo) }
    }

    @Test
    fun test_onReceive_withError_doesNothing() {
        val geofencingEvent = mockk<GeofencingEvent>(relaxed = true)
        every { geofencingEvent.hasError() } returns true

        mockkStatic(GeofencingEvent::class)
        every { GeofencingEvent.fromIntent(any()) } returns geofencingEvent

        val intent = Intent()

        receiver.onReceive(context, intent)

        coVerify(exactly = 0) { Repository.getMemoById(any()) }
        verify(exactly = 0) { NotificationUtils.createNotificationChannel(any()) }
        verify(exactly = 0) { NotificationUtils.showMemoNotification(any(), any()) }
    }
}
