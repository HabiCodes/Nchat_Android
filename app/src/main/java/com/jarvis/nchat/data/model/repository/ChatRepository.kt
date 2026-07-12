package com.jarvis.nchat.data.repository

import com.jarvis.nchat.core.network.ApiService
import com.jarvis.nchat.core.network.SocketManager
import com.jarvis.nchat.core.datastore.TokenDataStore
import com.jarvis.nchat.data.model.MessageDto
import com.jarvis.nchat.data.model.StartConversationRequest
import com.jarvis.nchat.domain.model.Conversation
import com.jarvis.nchat.domain.model.Message
import com.jarvis.nchat.domain.model.MessageDeliveryStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ApiService,
    private val socketManager: SocketManager,
    private val tokenDataStore: TokenDataStore,
) {
    suspend fun getConversations(): Result<List<Conversation>> = runCatching {
        api.getConversations().conversations.map {
            Conversation(
                id = it.conversation_id,
                otherUserId = it.other_user_id.orEmpty(),
                otherUsername = it.other_username ?: "Unknown",
                otherAvatarUrl = it.other_avatar_url,
                otherIsOnline = it.other_is_online ?: false,
                lastMessage = it.last_message_content,
                lastMessageAt = it.last_message_at,
            )
        }
    }

    suspend fun startConversation(otherUserId: String): Result<String> = runCatching {
        api.startConversation(StartConversationRequest(otherUserId)).conversationId
    }

    suspend fun getMessages(conversationId: String): Result<List<Message>> = runCatching {
        val myId = tokenDataStore.userId.first()
        api.getMessages(conversationId).messages.map { it.toDomain(myId) }
    }

    // Live stream of incoming messages for a specific conversation - filters the global socket flow
    fun observeIncomingMessages(conversationId: String): Flow<Message> {
        return socketManager.observeNewMessages()
            .map { dto -> dto }
            .let { flow ->
                kotlinx.coroutines.flow.flow {
                    val myId = tokenDataStore.userId.first()
                    flow.collect { dto ->
                        if (dto.conversation_id == conversationId) emit(dto.toDomain(myId))
                    }
                }
            }
    }

    suspend fun sendMessage(conversationId: String, content: String): Result<Message> = runCatching {
        val myId = tokenDataStore.userId.first()
        val clientMsgId = UUID.randomUUID().toString()
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            socketManager.sendMessage(conversationId, content, clientMsgId) { ack ->
                if (ack.has("error")) {
                    cont.resumeWith(Result.failure(Exception(ack.getString("error"))))
                } else {
                    val messageJson = ack.getJSONObject("message").toString()
                    val dto = com.google.gson.Gson().fromJson(messageJson, MessageDto::class.java)
                    cont.resumeWith(Result.success(dto.toDomain(myId)))
                }
            }
        }
    }

    fun markConversationRead(conversationId: String) = socketManager.markRead(conversationId)
    fun emitTyping(conversationId: String) = socketManager.emitTypingStart(conversationId)
}

private fun MessageDto.toDomain(myId: String?) = Message(
    id = id,
    conversationId = conversation_id,
    senderId = sender_id,
    content = content.orEmpty(),
    status = when (status) {
        "read" -> MessageDeliveryStatus.READ
        "delivered" -> MessageDeliveryStatus.DELIVERED
        else -> MessageDeliveryStatus.SENT
    },
    createdAt = created_at,
    isMine = sender_id == myId,
)