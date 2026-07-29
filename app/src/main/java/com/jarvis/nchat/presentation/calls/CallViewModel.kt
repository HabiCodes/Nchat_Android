package com.jarvis.nchat.presentation.calls

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val repo: CallSessionRepository,
) : ViewModel() {

    val uiState: StateFlow<CallUiState> = repo.uiState

    fun startCall(otherUserId: String, otherUsername: String, conversationId: String) =
        repo.startCall(otherUserId, otherUsername, conversationId)

    fun onIncomingCall(fromUserId: String, fromUsername: String, conversationId: String, callId: String?) =
        repo.onIncomingCall(fromUserId, fromUsername, conversationId, callId)

    fun acceptCall() = repo.acceptCall()
    fun rejectCall() = repo.rejectCall()
    fun endCall() = repo.endCall()
    fun toggleMute() = repo.toggleMute()
    fun toggleSpeaker() = repo.toggleSpeaker()
}