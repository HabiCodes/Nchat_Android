package com.jarvis.nchat.data.model

data class ConversationDto(
    val conversation_id: String,
    val is_group: Boolean,
    val name: String?,
    val other_user_id: String?,
    val other_username: String?,
    val other_avatar_url: String?,
    val other_is_online: Boolean?,
    val last_message_content: String?,
    val last_message_at: String?,
)
data class CallDto(
    val id: String,
    val call_type: String,
    val status: String,
    val started_at: String,
    val direction: String,
    val other_user_id: String,
    val other_username: String,
    val other_avatar_url: String?,
)

data class CallListResponse(val calls: List<CallDto>)

data class ConversationListResponse(val conversations: List<ConversationDto>)
data class StartConversationRequest(val otherUserId: String)
data class StartConversationResponse(val conversationId: String)