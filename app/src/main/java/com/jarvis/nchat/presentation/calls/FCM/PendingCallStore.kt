package com.jarvis.nchat.core.call

import javax.inject.Inject
import javax.inject.Singleton

data class PendingIncomingCall(
    val callId: String?,
    val fromUserId: String,
    val fromUsername: String,
    val conversationId: String,
    val callType: String,
)

@Singleton
class PendingCallStore @Inject constructor() {
    @Volatile
    var pending: PendingIncomingCall? = null

    fun consume(): PendingIncomingCall? {
        val value = pending
        pending = null
        return value
    }
}