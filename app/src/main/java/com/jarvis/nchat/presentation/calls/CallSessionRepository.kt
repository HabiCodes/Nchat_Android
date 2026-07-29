package com.jarvis.nchat.presentation.calls

import android.content.Context
import com.jarvis.nchat.core.call.CallForegroundService
import com.jarvis.nchat.core.call.PendingCallStore
import com.jarvis.nchat.core.datastore.TokenDataStore
import com.jarvis.nchat.core.network.CallAudioManager
import com.jarvis.nchat.core.network.CallManager
import com.jarvis.nchat.core.network.SocketManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton



enum class CallStatus { IDLE, RINGING_OUTGOING, RINGING_INCOMING, CONNECTING, CONNECTED, RECONNECTING, ENDED }

data class CallUiState(
    val status: CallStatus = CallStatus.IDLE,
    val otherUserId: String = "",
    val otherUsername: String = "",
    val conversationId: String = "",
    val callId: String? = null,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val durationSeconds: Int = 0,
    val errorMessage: String? = null,
)

/**
 * SINGLE source of truth for the live call — the entire app (in-app call
 * screen, lock-screen incoming-call Activity, notification, foreground
 * service) all observe THIS, none of them own their own call state.
 *
 * Listeners are registered exactly once, in [startListening], which must be
 * called from Application.onCreate() — never from a screen or ViewModel.
 * This is what prevents duplicate signaling handlers and split call state
 * across multiple CallViewModel instances.
 */
@Singleton
class CallSessionRepository @Inject constructor(
    private val callManager: CallManager,
    private val socketManager: SocketManager,
    private val callAudioManager: CallAudioManager,
    private val tokenDataStore: TokenDataStore,
    private val pendingCallStore: PendingCallStore,
    @ApplicationContext private val appContext: Context,
) {
    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var listenersStarted = false
    private var durationTimerJob: kotlinx.coroutines.Job? = null
    private var reconnectGraceJob: kotlinx.coroutines.Job? = null

    private var myUserId: String? = null

    fun startListening() {
        if (listenersStarted) return
        listenersStarted = true

        repoScope.launch { myUserId = tokenDataStore.userId.first() }

        listenForIncomingCalls()
        listenForCallAccepted()
        listenForCallRejected()
        listenForOffers()
        listenForAnswers()
        listenForIceCandidates()
        listenForCallEnded()
        listenForCallMissed()

        // Cold-start case: process was killed, FCM woke it up, PendingCallStore
        // holds the call that IncomingCallActivity's intent already carries too.
        // We consume it here just to make sure uiState reflects it immediately,
        // even if some screen reads uiState before the Activity's intent is processed.
        pendingCallStore.pending?.let { pending ->
            onIncomingCall(
                fromUserId = pending.fromUserId,
                fromUsername = pending.fromUsername,
                conversationId = pending.conversationId,
                callId = pending.callId,
            )
            pendingCallStore.pending = null
        }
    }

    // ---------------- Outgoing call ----------------
    fun startCall(otherUserId: String, otherUsername: String, conversationId: String) {
        if (_uiState.value.status != CallStatus.IDLE && _uiState.value.status != CallStatus.ENDED) {
            return // already in a call — don't stomp on it
        }
        callManager.initialize()
        _uiState.value = CallUiState(
            status = CallStatus.RINGING_OUTGOING,
            otherUserId = otherUserId, otherUsername = otherUsername, conversationId = conversationId,
        )
        callManager.createPeerConnection(peerObserver())
        socketManager.emitCallInvite(otherUserId, conversationId, "audio") { ack ->
            if (ack.optString("status") == "ringing") {
                val callId = ack.optString("callId").takeIf { it.isNotBlank() }
                _uiState.value = _uiState.value.copy(callId = callId)
            } else {
                _uiState.value = _uiState.value.copy(
                    status = CallStatus.ENDED,
                    errorMessage = ack.optString("error", "Call failed"),
                )
                cleanupAfterEnd()
            }
        }
    }

    // ---------------- Incoming call ----------------
    fun onIncomingCall(fromUserId: String, fromUsername: String, conversationId: String, callId: String?) {
        if (_uiState.value.status != CallStatus.IDLE && _uiState.value.status != CallStatus.ENDED) {
            return // already mid-call — real client would auto-reject as "busy" here
        }
        callManager.initialize()
        _uiState.value = CallUiState(
            status = CallStatus.RINGING_INCOMING,
            otherUserId = fromUserId, otherUsername = fromUsername, conversationId = conversationId, callId = callId,
        )
    }

    fun acceptCall() {
        val state = _uiState.value
        if (state.status != CallStatus.RINGING_INCOMING) return
        callManager.createPeerConnection(peerObserver())
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
        _uiState.value = _uiState.value.copy(status = CallStatus.ENDED)
        cleanupAfterEnd()
    }

    private fun cleanupAfterEnd() {
        callManager.endCall()
        callAudioManager.endCallAudio()
        durationTimerJob?.cancel()
        reconnectGraceJob?.cancel()
        stopCallForegroundService()
        // Reset to IDLE shortly after, so the UI has a beat to show "call ended"
        repoScope.launch {
            kotlinx.coroutines.delay(1500)
            if (_uiState.value.status == CallStatus.ENDED) {
                _uiState.value = CallUiState()
            }
        }
    }

    fun toggleMute() {
        val muted = !_uiState.value.isMuted
        callManager.toggleMute(muted)
        _uiState.value = _uiState.value.copy(isMuted = muted)
    }

    fun toggleSpeaker() {
        val speakerOn = !_uiState.value.isSpeakerOn
        callAudioManager.setSpeaker(speakerOn)
        _uiState.value = _uiState.value.copy(isSpeakerOn = speakerOn)
    }

    // ---------------- PeerConnection observer ----------------
    private fun peerObserver() = object : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate?) {
            candidate ?: return
            val state = _uiState.value
            socketManager.emitIceCandidate(state.otherUserId, state.callId, candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex)
        }

        override fun onConnectionChange(state: PeerConnection.PeerConnectionState?) {
            when (state) {
                PeerConnection.PeerConnectionState.CONNECTED -> {
                    reconnectGraceJob?.cancel()
                    if (_uiState.value.status != CallStatus.CONNECTED) {
                        _uiState.value = _uiState.value.copy(status = CallStatus.CONNECTED)
                        callAudioManager.startCallAudio()
                        startDurationTimer()
                    }
                }
                PeerConnection.PeerConnectionState.DISCONNECTED -> {
                    // brief network hiccup — give it a grace window before treating as failed
                    if (_uiState.value.status == CallStatus.CONNECTED) {
                        _uiState.value = _uiState.value.copy(status = CallStatus.RECONNECTING)
                        startReconnectGraceTimer()
                    }
                }
                PeerConnection.PeerConnectionState.FAILED -> {
                    attemptIceRestart()
                }
                else -> {}
            }
        }

        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
        override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
        override fun onIceConnectionReceivingChange(receiving: Boolean) {}
        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
        override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
        override fun onAddStream(stream: MediaStream?) {}
        override fun onRemoveStream(stream: MediaStream?) {}
        override fun onDataChannel(channel: org.webrtc.DataChannel?) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
    }

    private fun startReconnectGraceTimer() {
        reconnectGraceJob?.cancel()
        reconnectGraceJob = repoScope.launch {
            kotlinx.coroutines.delay(10_000) // 10s grace before giving up on a dropped connection
            if (_uiState.value.status == CallStatus.RECONNECTING) {
                attemptIceRestart()
            }
        }
    }

    private fun attemptIceRestart() {
        val state = _uiState.value
        if (state.otherUserId.isEmpty()) return
        callManager.createOffer(
            iceRestart = true,
            onSuccess = { sdp ->
                socketManager.emitCallOffer(state.otherUserId, state.callId, sdp.description, sdp.type.canonicalForm())
            },
            onFailure = {
                // ICE restart failed too — give up on the call
                _uiState.value = _uiState.value.copy(status = CallStatus.ENDED, errorMessage = "Connection lost")
                cleanupAfterEnd()
            },
        )
    }

    private fun startDurationTimer() {
        durationTimerJob?.cancel()
        durationTimerJob = repoScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                _uiState.value = _uiState.value.copy(durationSeconds = _uiState.value.durationSeconds + 1)
            }
        }
    }

    // ---------------- Socket event listeners (registered exactly once) ----------------
    private fun listenForIncomingCalls() = repoScope.launch {
        socketManager.observeCallIncoming().collect { data ->
            onIncomingCall(
                fromUserId = data.getString("fromUserId"),
                fromUsername = data.optString("fromUsername", "Unknown"),
                conversationId = data.optString("conversationId"),
                callId = data.optString("callId").takeIf { it.isNotBlank() },
            )
        }
    }

    private fun listenForCallAccepted() = repoScope.launch {
        socketManager.observeCallAccepted().collect {
            val state = _uiState.value
            _uiState.value = state.copy(status = CallStatus.CONNECTING)
            callManager.createOffer(
                onSuccess = { sdp ->
                    socketManager.emitCallOffer(state.otherUserId, state.callId, sdp.description, sdp.type.canonicalForm())
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(status = CallStatus.ENDED, errorMessage = err)
                    cleanupAfterEnd()
                },
            )
        }
    }

    private fun listenForCallRejected() = repoScope.launch {
        socketManager.observeCallRejected().collect { endCallLocally() }
    }

    private fun listenForOffers() = repoScope.launch {
        socketManager.observeCallOffer().collect { data ->
            runCatching {
                val sdpObj = data.getJSONObject("sdp")
                val offer = SessionDescription(SessionDescription.Type.OFFER, sdpObj.getString("sdp"))
                callManager.setRemoteDescription(offer) { success, error ->
                    if (!success) return@setRemoteDescription
                    callManager.createAnswer(
                        onSuccess = { answer ->
                            val state = _uiState.value
                            socketManager.emitCallAnswer(state.otherUserId, state.callId, answer.description, answer.type.canonicalForm())
                        },
                        onFailure = { /* log; call will simply hang and time out */ },
                    )
                }
            }
        }
    }

    private fun listenForAnswers() = repoScope.launch {
        socketManager.observeCallAnswer().collect { data ->
            runCatching {
                val sdpObj = data.getJSONObject("sdp")
                val answer = SessionDescription(SessionDescription.Type.ANSWER, sdpObj.getString("sdp"))
                callManager.setRemoteDescription(answer) { _, _ -> }
            }
        }
    }

    private fun listenForIceCandidates() = repoScope.launch {
        socketManager.observeCallIceCandidate().collect { data ->
            runCatching {
                val c = data.getJSONObject("candidate")
                val candidate = IceCandidate(c.optString("sdpMid"), c.optInt("sdpMLineIndex"), c.getString("candidate"))
                callManager.addIceCandidate(candidate)
            }
        }
    }

    private fun listenForCallEnded() = repoScope.launch {
        socketManager.observeCallEnded().collect { endCallLocally() }
    }

    private fun listenForCallMissed() = repoScope.launch {
        socketManager.observeCallMissed().collect { endCallLocally() }
    }

    private fun stopCallForegroundService() {
        appContext.stopService(
            android.content.Intent(appContext, CallForegroundService::class.java)
        )
    }
}