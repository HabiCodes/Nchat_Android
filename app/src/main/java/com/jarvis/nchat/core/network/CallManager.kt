package com.jarvis.nchat.core.network

import android.content.Context
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class CallManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var audioSource: AudioSource? = null
    private val eglBase: EglBase = EglBase.create()

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:openrelay.metered.ca:80").createIceServer(),
        PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
            .setUsername("openrelayproject").setPassword("openrelayproject").createIceServer(),
        PeerConnection.IceServer.builder("turn:openrelay.metered.ca:443")
            .setUsername("openrelayproject").setPassword("openrelayproject").createIceServer(),
    )

    fun initialize() {
        if (peerConnectionFactory != null) return // already initialized

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

    // Creates the peer connection for a new call. Must call initialize() first.
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

    fun createOffer(onSuccess: (SessionDescription) -> Unit, onFailure: (String) -> Unit) {
        val constraints = MediaConstraints()
        peerConnection?.createOffer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp ?: return
                peerConnection?.setLocalDescription(SdpObserverAdapter(), sdp)
                onSuccess(sdp)
            }
            override fun onCreateFailure(error: String?) {
                onFailure(error ?: "Unknown error creating offer")
            }
        }, constraints)
    }

    fun createAnswer(onSuccess: (SessionDescription) -> Unit, onFailure: (String) -> Unit) {
        val constraints = MediaConstraints()
        peerConnection?.createAnswer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp ?: return
                peerConnection?.setLocalDescription(SdpObserverAdapter(), sdp)
                onSuccess(sdp)
            }
            override fun onCreateFailure(error: String?) {
                onFailure(error ?: "Unknown error creating answer")
            }
        }, constraints)
    }



    fun setRemoteDescription(sdp: SessionDescription) {
        peerConnection?.setRemoteDescription(SdpObserverAdapter(), sdp)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun toggleMute(muted: Boolean) {
        localAudioTrack?.setEnabled(!muted)
    }

    fun toggleSpeaker(context: Context, speakerOn: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        audioManager.isSpeakerphoneOn = speakerOn
    }

    fun endCall() {
        peerConnection?.close()
        peerConnection = null
        localAudioTrack = null
        audioSource?.dispose()
        audioSource = null
    }
}

// Base adapter so we only override the callbacks we actually need per call-site
open class SdpObserverAdapter : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) {}
}