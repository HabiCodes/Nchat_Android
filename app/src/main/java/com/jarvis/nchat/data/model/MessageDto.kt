package com.jarvis.nchat.data.model

data class MessageDto(
    val id: String,
    val conversation_id: String,
    val sender_id: String,
    val content: String?,
    val message_type: String,
    val media_url: String?,
    val status: String,
    val created_at: String,
)

data class MessageListResponse(val messages: List<MessageDto>)