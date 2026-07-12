package com.jarvis.nchat.domain.model

enum class MessageDeliveryStatus { SENDING, SENT, DELIVERED, READ }

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val status: MessageDeliveryStatus,
    val createdAt: String,
    val isMine: Boolean,
)