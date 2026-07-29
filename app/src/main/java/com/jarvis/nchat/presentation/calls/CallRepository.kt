package com.jarvis.nchat.data.repository

import com.jarvis.nchat.core.network.ApiService
import com.jarvis.nchat.presentation.calls.CallDirection
import com.jarvis.nchat.presentation.calls.CallKind
import com.jarvis.nchat.presentation.calls.CallLogEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallHistoryRepository @Inject constructor(private val api: ApiService) {
    suspend fun getCallHistory(): Result<List<CallLogEntry>> = runCatching {
        api.getCalls().calls.map {
            CallLogEntry(
                id = it.id,
                name = it.other_username ?: "Unknown", // FIXED: null username would otherwise crash CallLogEntry's UI (Text(null) throws)
                avatarUrl = it.other_avatar_url,
                otherUserId = it.other_user_id,
                direction = when {
                    it.status == "missed" || it.status == "rejected" -> CallDirection.MISSED
                    it.direction == "outgoing" -> CallDirection.OUTGOING
                    else -> CallDirection.INCOMING
                },
                kind = if (it.call_type == "video") CallKind.VIDEO else CallKind.AUDIO,
                time = it.started_at,
            )
        }
    }
}