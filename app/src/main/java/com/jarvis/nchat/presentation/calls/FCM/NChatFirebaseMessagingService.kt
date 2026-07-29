package com.jarvis.nchat.core.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jarvis.nchat.R
import com.jarvis.nchat.core.call.CallForegroundService
import com.jarvis.nchat.core.call.PendingCallStore
import com.jarvis.nchat.core.call.PendingIncomingCall
import com.jarvis.nchat.core.datastore.TokenDataStore
import com.jarvis.nchat.data.repository.AuthRepository
import com.jarvis.nchat.presentation.calls.IncomingCallActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CALL_CHANNEL_ID = "nchat_incoming_calls"
private const val CALL_NOTIFICATION_ID = 9001

/**
 * IMPORTANT: this must be a **data message** from your backend (only a
 * `"data"` field, no `"notification"` field in the FCM payload). A payload
 * that includes `"notification"` gets delivered straight to the system tray
 * by the OS when the app is backgrounded/killed, and `onMessageReceived`
 * is never called — which is almost certainly why calls "don't work
 * properly" today if your backend is sending notification-type pushes.
 */
@AndroidEntryPoint
class NChatFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var pendingCallStore: PendingCallStore
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var tokenDataStore: TokenDataStore

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        when (data["type"]) {
            "incoming_call" -> handleIncomingCall(data)
            // add other push types here later (e.g. "new_message") without
            // touching the call-handling path.
        }
    }

    override fun onNewToken(token: String) {
        // Fire-and-forget register with backend whenever the token rotates
        // (fresh install, app data cleared, or periodic OS-driven rotation).
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { authRepository.registerFcmToken(token) }
        }
    }

    private fun handleIncomingCall(data: Map<String, String>) {
        val fromUserId = data["fromUserId"] ?: return
        val callId = data["callId"]
        val fromUsername = data["fromUsername"] ?: "Unknown"
        val conversationId = data["conversationId"] ?: return
        val callType = data["callType"] ?: "audio"

        pendingCallStore.pending = PendingIncomingCall(
            callId = callId,
            fromUserId = fromUserId,
            fromUsername = fromUsername,
            conversationId = conversationId,
            callType = callType,
        )

        ensureNotificationChannel()

        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(IncomingCallActivity.EXTRA_FROM_USER_ID, fromUserId)
            putExtra(IncomingCallActivity.EXTRA_FROM_USERNAME, fromUsername)
            putExtra(IncomingCallActivity.EXTRA_CONVERSATION_ID, conversationId)
            putExtra(IncomingCallActivity.EXTRA_CALL_ID, callId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, CALL_NOTIFICATION_ID, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CALL_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Incoming call")
            .setContentText(fromUsername)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        // Foreground service keeps the process alive long enough to reconnect
        // the socket and hold the call state while the full-screen UI shows.
        val serviceIntent = Intent(this, CallForegroundService::class.java).apply {
            putExtra(CallForegroundService.EXTRA_NOTIFICATION_ID, CALL_NOTIFICATION_ID)
        }
        ContextCompat.startForegroundService(this, serviceIntent)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CALL_CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CALL_CHANNEL_ID,
            "Incoming calls",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notifications for incoming NChat calls"
            enableVibration(true)
            setBypassDnd(true)
        }
        manager.createNotificationChannel(channel)
    }
}