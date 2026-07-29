package com.jarvis.nchat.core.call

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jarvis.nchat.R
import com.jarvis.nchat.core.datastore.TokenDataStore
import com.jarvis.nchat.core.network.SocketManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.cancel

/**
 * Keeps the process alive during an incoming call and makes sure the socket
 * is connected so Accept/Reject/offer-answer signaling works the moment the
 * user responds — even if the app process was fully killed when the push
 * arrived. Started by [com.jarvis.nchat.core.fcm.NChatFirebaseMessagingService];
 * stopped by [com.jarvis.nchat.presentation.calls.CallViewModel] once the
 * call ends (accepted-and-connected calls hand off to the normal in-app flow;
 * this service's job is done once the socket is confirmed connected).
 */
@AndroidEntryPoint
class CallForegroundService : Service() {

    @Inject lateinit var socketManager: SocketManager
    @Inject lateinit var tokenDataStore: TokenDataStore

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationId = intent?.getIntExtra(EXTRA_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID)
            ?: DEFAULT_NOTIFICATION_ID

        startForeground(notificationId, buildMinimalNotification())

        scope.launch {
            val token = tokenDataStore.token.first()
            if (token != null) {
                socketManager.forceReconnect(token)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    /**
     * Android requires startForeground() to be called with a real
     * notification within a few seconds of service start. The FCM service
     * already posts the full, user-visible call notification separately —
     * this one is a minimal placeholder satisfying the same channel/ID so
     * the two don't visually conflict.
     */
    private fun buildMinimalNotification(): Notification {
        return NotificationCompat.Builder(this, "nchat_incoming_calls")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("NChat")
            .setContentText("Call in progress")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        private const val DEFAULT_NOTIFICATION_ID = 9001
    }
}