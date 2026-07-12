package com.jarvis.nchat.domain.model

data class Conversation(
    val id: String,
    val otherUserId: String,
    val otherUsername: String,
    val otherAvatarUrl: String?,
    val otherIsOnline: Boolean,
    val lastMessage: String?,
    val lastMessageAt: String?,
)