package com.sap.codelab.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.notification.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            return
        }

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val requestIdString = geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId ?: return
            val requestIdLong = requestIdString.toLongOrNull() ?: return

            receiverScope.launch {
                try {
                    val memo = Repository.getMemoById(requestIdLong)
                    NotificationUtils.createNotificationChannel(context)
                    NotificationUtils.showMemoNotification(context, memo)
                } catch (_: Exception) {
                }
            }
        }
    }
}
