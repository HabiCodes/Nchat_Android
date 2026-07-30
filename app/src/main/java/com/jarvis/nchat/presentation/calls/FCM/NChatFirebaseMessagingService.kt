package com.jarvis.nchat.core.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jarvis.nchat.MainActivity
import com.jarvis.nchat.R
import com.jarvis.nchat.core.chat.ActiveConversationTracker
import com.jarvis.nchat.core.chat.AppForegroundTracker
import com.jarvis.nchat.data.repository.AuthRepository
import com.jarvis.nchat.presentation.calls.CallSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MESSAGE_CHANNEL_ID = "nchat_messages"

/**
 * IMPORTANT: this must be a **data message** from your backend (only a
 * `"data"` field, no `"notification"` field in the FCM payload) — otherwise
 * the OS delivers it straight to the system tray and onMessageReceived
 * never runs.
 */
@AndroidEntryPoint
class NChatFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var callSessionRepository: CallSessionRepository
    @Inject lateinit var activeConversationTracker: ActiveConversationTracker
    @Inject lateinit var appForegroundTracker: AppForegroundTracker

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        when (data["type"]) {
            "incoming_call" -> handleIncomingCall(data)
            "new_message" -> handleNewMessage(data)
        }
    }

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { authRepository.registerFcmToken(token) }
        }
    }

    private fun handleIncomingCall(data: Map<String, String>) {
        val fromUserId = data["fromUserId"] ?: return
        val callId = data["callId"]
        val fromUsername = data["fromUsername"] ?: "Unknown"
        val conversationId = data["conversationId"] ?: return

        // Single source of truth — starts the ringtone, shows the
        // notification, arms the timeout. Safe to call even if a socket
        // 'call:incoming' already triggered the same thing (idempotent).
        callSessionRepository.onIncomingCall(fromUserId, fromUsername, conversationId, callId)
    }

    private fun handleNewMessage(data: Map<String, String>) {
        val conversationId = data["conversationId"] ?: return

        // Already visible on screen, or the app is foregrounded (the socket
        // path / MessageNotificationManager already handles that case) —
        // don't double-notify.
        if (activeConversationTracker.activeConversationId == conversationId) return
        if (appForegroundTracker.isForeground) return

        val fromUsername = data["fromUsername"] ?: "New message"
        val preview = data["preview"] ?: "Sent a message"

        ensureMessageChannel()

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversationId", conversationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, conversationId.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, MESSAGE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(fromUsername)
            .setContentText(preview)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(conversationId)
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(conversationId.hashCode(), notification)
    }

    private fun ensureMessageChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(MESSAGE_CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(MESSAGE_CHANNEL_ID, "Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "New message notifications"
                enableVibration(true)
            }
        )
    }
}