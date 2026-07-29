package com.jarvis.nchat.core.network

import android.content.Context
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var audioSource: AudioSource? = null
    private val eglBase: EglBase = EglBase.create()

    // NEW: queue for ICE candidates that arrive before setRemoteDescription finishes
    private val pendingRemoteCandidates = mutableListOf<IceCandidate>()
    private var remoteDescriptionSet = false

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:openrelay.metered.ca:80").createIceServer(),
        PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
            .setUsername("openrelayproject").setPassword("openrelayproject").createIceServer(),
        PeerConnection.IceServer.builder("turn:openrelay.metered.ca:443")
            .setUsername("openrelayproject").setPassword("openrelayproject").createIceServer(),
    )
    // ⚠️ Reminder: replace this with your own coturn server + short-lived
    // credentials before you have real users. This public relay will not
    // hold up in production and the credentials are visible in your APK.

    fun initialize() {
        if (peerConnectionFactory != null) return

        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        peerConnection = peerConnectionFactory?.createPeerConnection(config, observer)
        attachLocalAudio()
        return peerConnection
    }

    private fun attachLocalAudio() {
        val audioConstraints = MediaConstraints()
        audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("local_audio_track", audioSource)
        localAudioTrack?.let { track ->
            peerConnection?.addTrack(track, listOf("local_stream"))
        }
    }

    // FIXED: now waits for setLocalDescription to actually succeed before
    // telling the caller "here's your SDP, go send it" — previously we sent
    // it to the signaling layer before confirming it was even applied locally.
    fun createOffer(
        iceRestart: Boolean = false,
        onSuccess: (SessionDescription) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val constraints = MediaConstraints().apply {
            if (iceRestart) mandatory.add(MediaConstraints.KeyValuePair("IceRestart", "true"))
        }
        peerConnection?.createOffer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp ?: return onFailure("Null SDP from createOffer")
                peerConnection?.setLocalDescription(object : SdpObserverAdapter() {
                    override fun onSetSuccess() = onSuccess(sdp)
                    override fun onSetFailure(error: String?) =
                        onFailure(error ?: "setLocalDescription failed")
                }, sdp)
            }
            override fun onCreateFailure(error: String?) =
                onFailure(error ?: "Unknown error creating offer")
        }, constraints)
    }

    fun createAnswer(onSuccess: (SessionDescription) -> Unit, onFailure: (String) -> Unit) {
        val constraints = MediaConstraints()
        peerConnection?.createAnswer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp ?: return onFailure("Null SDP from createAnswer")
                peerConnection?.setLocalDescription(object : SdpObserverAdapter() {
                    override fun onSetSuccess() = onSuccess(sdp)
                    override fun onSetFailure(error: String?) =
                        onFailure(error ?: "setLocalDescription failed")
                }, sdp)
            }
            override fun onCreateFailure(error: String?) =
                onFailure(error ?: "Unknown error creating answer")
        }, constraints)
    }

    // FIXED: now flushes any ICE candidates that arrived early, and tells
    // the caller whether it actually worked (silent failure was the old bug).
    fun setRemoteDescription(sdp: SessionDescription, onDone: (Boolean, String?) -> Unit) {
        peerConnection?.setRemoteDescription(object : SdpObserverAdapter() {
            override fun onSetSuccess() {
                remoteDescriptionSet = true
                synchronized(pendingRemoteCandidates) {
                    pendingRemoteCandidates.forEach { peerConnection?.addIceCandidate(it) }
                    pendingRemoteCandidates.clear()
                }
                onDone(true, null)
            }
            override fun onSetFailure(error: String?) {
                onDone(false, error)
            }
        }, sdp)
    }

    // FIXED: queues candidates instead of dropping/failing them if they
    // arrive before setRemoteDescription has completed.
    fun addIceCandidate(candidate: IceCandidate) {
        if (remoteDescriptionSet) {
            peerConnection?.addIceCandidate(candidate)
        } else {
            synchronized(pendingRemoteCandidates) { pendingRemoteCandidates.add(candidate) }
        }
    }

    fun toggleMute(muted: Boolean) {
        localAudioTrack?.setEnabled(!muted)
    }

    // NOTE: actual speaker routing now lives in CallAudioManager (below).
    // This is kept only if you still want a manual fallback path.
    fun toggleSpeakerLegacy(context: Context, speakerOn: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        audioManager.isSpeakerphoneOn = speakerOn
    }

    // FIXED: added .dispose() calls — the old version leaked native WebRTC
    // memory on every single call (close() alone doesn't free native memory).
    fun endCall() {
        peerConnection?.close()
        peerConnection?.dispose()
        peerConnection = null

        localAudioTrack?.dispose()
        localAudioTrack = null

        audioSource?.dispose()
        audioSource = null

        remoteDescriptionSet = false
        synchronized(pendingRemoteCandidates) { pendingRemoteCandidates.clear() }
    }
}

open class SdpObserverAdapter : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) {}
}