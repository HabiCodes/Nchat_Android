package com.jarvis.nchat.presentation.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.nchat.core.datastore.TokenDataStore
import com.jarvis.nchat.core.network.CallManager
import com.jarvis.nchat.core.network.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.webrtc.*
import javax.inject.Inject

enum class CallStatus { IDLE, RINGING_OUTGOING, RINGING_INCOMING, CONNECTING, CONNECTED, ENDED }

data class CallUiState(
    val status: CallStatus = CallStatus.IDLE,
    val otherUserId: String = "",
    val otherUsername: String = "",
    val conversationId: String = "",
    val callId: String? = null,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val durationSeconds: Int = 0,
)

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callManager: CallManager,
    private val socketManager: SocketManager,
    private val tokenDataStore: TokenDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState

    private var myUserId: String? = null

    init {
        viewModelScope.launch { myUserId = tokenDataStore.userId.first() }
        listenForIncomingCalls()
        listenForCallAccepted()
        listenForCallRejected()
        listenForOffers()
        listenForAnswers()
        listenForIceCandidates()
        listenForCallEnded()
    }

    // ---- Outgoing call ----
    fun startCall(otherUserId: String, otherUsername: String, conversationId: String) {
        callManager.initialize()
        _uiState.value = CallUiState(
            status = CallStatus.RINGING_OUTGOING,
            otherUserId = otherUserId, otherUsername = otherUsername, conversationId = conversationId
        )
        callManager.createPeerConnection(peerObserver(otherUserId))
        socketManager.emitCallInvite(otherUserId, conversationId, "audio") { ack ->
            if (ack.optString("status") == "ringing") {
                val callId = ack.optString("callId").takeIf { it.isNotBlank() }
                _uiState.value = _uiState.value.copy(callId = callId)
            } else {
                _uiState.value = _uiState.value.copy(status = CallStatus.ENDED)
            }
        }
    }

    // ---- Incoming call ----
    fun onIncomingCall(fromUserId: String, fromUsername: String, conversationId: String, callId: String?) {
        callManager.initialize()
        _uiState.value = CallUiState(
            status = CallStatus.RINGING_INCOMING,
            otherUserId = fromUserId, otherUsername = fromUsername, conversationId = conversationId, callId = callId
        )
    }

    fun acceptCall() {
        val state = _uiState.value
        callManager.createPeerConnection(peerObserver(state.otherUserId))
        socketManager.emitCallAccept(state.otherUserId, state.conversationId, state.callId)
        _uiState.value = state.copy(status = CallStatus.CONNECTING)
    }

    fun rejectCall() {
        val state = _uiState.value
        socketManager.emitCallReject(state.otherUserId, state.conversationId, state.callId)
        endCallLocally()
    }

    fun endCall() {
        val state = _uiState.value
        if (state.otherUserId.isNotEmpty()) {
            socketManager.emitCallEnd(state.otherUserId, state.conversationId, state.callId)
        }
        endCallLocally()
    }

    private fun endCallLocally() {
        callManager.endCall()
        _uiState.value = _uiState.value.copy(status = CallStatus.ENDED)
    }

    fun toggleMute() {
        val muted = !_uiState.value.isMuted
        callManager.toggleMute(muted)
        _uiState.value = _uiState.value.copy(isMuted = muted)
    }

    fun toggleSpeaker(context: android.content.Context) {
        val speakerOn = !_uiState.value.isSpeakerOn
        callManager.toggleSpeaker(context, speakerOn)
        _uiState.value = _uiState.value.copy(isSpeakerOn = speakerOn)
    }

    // ---- WebRTC peer connection observer ----
    private fun peerObserver(otherUserId: String) = object : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate?) {
            candidate ?: return
            socketManager.emitIceCandidate(otherUserId, candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex)
        }
        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
            if (state == PeerConnection.IceConnectionState.CONNECTED) {
                _uiState.value = _uiState.value.copy(status = CallStatus.CONNECTED)
            }
        }
        override fun onConnectionChange(state: PeerConnection.PeerConnectionState?) {}
        override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
        override fun onIceConnectionReceivingChange(receiving: Boolean) {}
        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
        override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
        override fun onAddStream(stream: MediaStream?) {}
        override fun onRemoveStream(stream: MediaStream?) {}
        override fun onDataChannel(channel: DataChannel?) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
    }

    // ---- Socket event listeners ----
    private fun listenForIncomingCalls() = viewModelScope.launch {
        socketManager.observeCallIncoming().collect { data ->
            onIncomingCall(
                fromUserId = data.getString("fromUserId"),
                fromUsername = "Someone", // TODO: look up real username, backend only sends id
                conversationId = data.optString("conversationId"),
                callId = data.optString("callId").takeIf { it.isNotBlank() }
            )
        }
    }

    private fun listenForCallAccepted() = viewModelScope.launch {
        socketManager.observeCallAccepted().collect {
            _uiState.value = _uiState.value.copy(status = CallStatus.CONNECTING)
            callManager.createOffer(
                onSuccess = { sdp -> socketManager.emitCallOffer(_uiState.value.otherUserId, sdp.description, sdp.type.canonicalForm()) },
                onFailure = { }
            )
        }
    }

    private fun listenForCallRejected() = viewModelScope.launch {
        socketManager.observeCallRejected().collect { endCallLocally() }
    }

    private fun listenForOffers() = viewModelScope.launch {
        socketManager.observeCallOffer().collect { data ->
            val sdpObj = data.getJSONObject("sdp")
            val offer = SessionDescription(SessionDescription.Type.OFFER, sdpObj.getString("sdp"))
            callManager.setRemoteDescription(offer)
            callManager.createAnswer(
                onSuccess = { answer -> socketManager.emitCallAnswer(_uiState.value.otherUserId, answer.description, answer.type.canonicalForm()) },
                onFailure = { }
            )
        }
    }

    private fun listenForAnswers() = viewModelScope.launch {
        socketManager.observeCallAnswer().collect { data ->
            val sdpObj = data.getJSONObject("sdp")
            val answer = SessionDescription(SessionDescription.Type.ANSWER, sdpObj.getString("sdp"))
            callManager.setRemoteDescription(answer)
        }
    }

    private fun listenForIceCandidates() = viewModelScope.launch {
        socketManager.observeCallIceCandidate().collect { data ->
            val c = data.getJSONObject("candidate")
            val candidate = IceCandidate(c.optString("sdpMid"), c.optInt("sdpMLineIndex"), c.getString("candidate"))
            callManager.addIceCandidate(candidate)
        }
    }

    private fun listenForCallEnded() = viewModelScope.launch {
        socketManager.observeCallEnded().collect { endCallLocally() }
    }
}