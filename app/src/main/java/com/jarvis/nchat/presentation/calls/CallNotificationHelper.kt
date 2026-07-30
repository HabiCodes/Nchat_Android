package com.jarvis.nchat.core.call

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.jarvis.nchat.R
import com.jarvis.nchat.presentation.calls.IncomingCallActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CALL_CHANNEL_ID = "nchat_incoming_calls"
const val CALL_NOTIFICATION_ID = 9001


@Singleton

class CallNotificationHelper @Inject constructor(@ApplicationContext private val context: Context) {


    fun showIncomingCall(call: PendingIncomingCall) {
        ensureNotificationChannel()

        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(IncomingCallActivity.EXTRA_FROM_USER_ID, call.fromUserId)
            putExtra(IncomingCallActivity.EXTRA_FROM_USERNAME, call.fromUsername)
            putExtra(IncomingCallActivity.EXTRA_CONVERSATION_ID, call.conversationId)
            putExtra(IncomingCallActivity.EXTRA_CALL_ID, call.callId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, CALL_NOTIFICATION_ID, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CALL_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Incoming call")
            .setContentText(call.fromUsername)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        if (hasMic) {
            val serviceIntent = Intent(context, CallForegroundService::class.java).apply {
                putExtra(CallForegroundService.EXTRA_NOTIFICATION_ID, CALL_NOTIFICATION_ID)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        context.getSystemService(NotificationManager::class.java).notify(CALL_NOTIFICATION_ID, notification)
    }

    fun clearIncomingCall() {
        context.getSystemService(NotificationManager::class.java).cancel(CALL_NOTIFICATION_ID)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CALL_CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CALL_CHANNEL_ID, "Incoming calls", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for incoming NChat calls"
                enableVibration(true)
                setBypassDnd(true)
            }
        )
    }
}