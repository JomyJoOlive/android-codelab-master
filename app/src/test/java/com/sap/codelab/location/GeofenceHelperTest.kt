package com.sap.codelab.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class GeofenceHelperTest {

    private lateinit var context: Context
    private lateinit var geofencingClient: GeofencingClient

    @Before
    fun setup() {
        context = spy(RuntimeEnvironment.getApplication())
        geofencingClient = mockk()

        mockkStatic(LocationServices::class)
        every { LocationServices.getGeofencingClient(context) } returns geofencingClient
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `addGeofence does nothing if fine location permission is denied`() {
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED

        GeofenceHelper.addGeofence(context, 10.0, 20.0, "123", "Title", "Description")

        verify(exactly = 0) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun `addGeofence does nothing if background location permission is denied`() {
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } returns PackageManager.PERMISSION_DENIED

        GeofenceHelper.addGeofence(context, 10.0, 20.0, "123", "Title", "Description")

        verify(exactly = 0) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun `addGeofence adds geofence when permissions granted`() {
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED

        every {
            geofencingClient.addGeofences(any(), any())
        } returns mockk()

        GeofenceHelper.addGeofence(context, 10.0, 20.0, "123", "Title", "Description")

        verify(exactly = 1) { geofencingClient.addGeofences(any(), any()) }
    }
}
