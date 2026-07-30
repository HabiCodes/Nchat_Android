package com.jarvis.nchat.core.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jarvis.nchat.MainActivity
import com.jarvis.nchat.R
import com.jarvis.nchat.core.datastore.TokenDataStore
import com.jarvis.nchat.core.network.SocketManager
import com.jarvis.nchat.data.repository.ChatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val MESSAGE_CHANNEL_ID = "nchat_messages"

data class InAppMessageBanner(val fromUsername: String, val preview: String, val conversationId: String)

@Singleton
class MessageNotificationManager @Inject constructor(
    private val socketManager: SocketManager,
    private val tokenDataStore: TokenDataStore,
    private val chatRepository: ChatRepository,
    private val activeConversationTracker: ActiveConversationTracker,
    private val appForegroundTracker: AppForegroundTracker,
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var started = false
    private val usernameCache = mutableMapOf<String, String>()

    private val _banners = MutableSharedFlow<InAppMessageBanner>(extraBufferCapacity = 1)
    val banners: SharedFlow<InAppMessageBanner> = _banners

    fun startListening() {
        if (started) return
        started = true
        scope.launch {
            val myId = tokenDataStore.userId.first()
            socketManager.observeNewMessages().collect { dto ->
                if (dto.sender_id == myId) return@collect
                if (activeConversationTracker.activeConversationId == dto.conversation_id) return@collect

                val fromUsername = resolveUsername(dto.conversation_id)
                val preview = dto.content ?: "Sent a message"

                if (appForegroundTracker.isForeground) {
                    _banners.tryEmit(InAppMessageBanner(fromUsername, preview, dto.conversation_id))
                } else {
                    showSystemNotification(fromUsername, preview, dto.conversation_id)
                }
            }
        }
    }

    private suspend fun resolveUsername(conversationId: String): String {
        usernameCache[conversationId]?.let { return it }
        // Note: refetches the whole conversation list on a cache miss — fine for
        // now, worth caching server-side or adding a single-conversation
        // endpoint later if this gets called often.
        val name = chatRepository.getConversations().getOrNull()
            ?.find { it.id == conversationId }?.otherUsername ?: "New message"
        usernameCache[conversationId] = name
        return name
    }

    private fun showSystemNotification(fromUsername: String, preview: String, conversationId: String) {
        ensureChannel()
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversationId", conversationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, conversationId.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, MESSAGE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(fromUsername)
            .setContentText(preview)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(conversationId)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(conversationId.hashCode(), notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(MESSAGE_CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(MESSAGE_CHANNEL_ID, "Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "New message notifications"
                enableVibration(true)
            }
        )
    }
}