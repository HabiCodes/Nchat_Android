package com.jarvis.nchat.core.network

import com.jarvis.nchat.data.model.MessageDto
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private const val SOCKET_URL = "https://n-backend-6xhg.onrender.com"

@Singleton
class SocketManager @Inject constructor() {
    private var socket: Socket? = null
    private val gson = Gson()

    // Listeners registered before the socket exists get queued here, then
    // attached the moment connect() creates the real Socket object - this is
    // what fixes calls/messages never arriving after a fresh login.
    private val pendingListeners = mutableListOf<Pair<String, Emitter.Listener>>()

    fun connect(token: String) {
        if (socket?.connected() == true) return
        val options = IO.Options().apply {
            auth = mapOf("token" to token)
            transports = arrayOf("websocket")
        }
        socket = IO.socket(SOCKET_URL, options)
        pendingListeners.forEach { (event, listener) -> socket?.on(event, listener) }
        socket?.connect()
    }
    fun forceReconnect(token: String) {
        disconnect()
        connect(token)
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        pendingListeners.clear()
    }

    private fun addListener(event: String, listener: Emitter.Listener) {
        pendingListeners.add(event to listener)
        socket?.on(event, listener)
    }

    private fun removeListener(event: String, listener: Emitter.Listener) {
        pendingListeners.removeAll { it.first == event && it.second === listener }
        socket?.off(event, listener)
    }

    private fun observeSocketEvent(event: String): Flow<JSONObject> = callbackFlow {
        val listener = Emitter.Listener { args -> trySend(args[0] as JSONObject) }
        addListener(event, listener)
        awaitClose { removeListener(event, listener) }
    }

    fun observeNewMessages(): Flow<MessageDto> = callbackFlow {
        val listener = Emitter.Listener { args ->
            val data = args[0] as JSONObject
            val messageJson = data.getJSONObject("message").toString()
            trySend(gson.fromJson(messageJson, MessageDto::class.java))
        }
        addListener("message:new", listener)
        awaitClose { removeListener("message:new", listener) }
    }

    fun sendMessage(conversationId: String, content: String, clientMsgId: String, onAck: (JSONObject) -> Unit) {
        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("content", content)
            put("clientMsgId", clientMsgId)
        }
        socket?.emit("message:send", payload, io.socket.client.Ack { args -> onAck(args[0] as JSONObject) })
    }

    fun markRead(conversationId: String) {
        socket?.emit("message:read", JSONObject().apply { put("conversationId", conversationId) })
    }

    fun emitTypingStart(conversationId: String) {
        socket?.emit("typing:start", JSONObject().apply { put("conversationId", conversationId) })
    }

    fun emitTypingStop(conversationId: String) {
        socket?.emit("typing:stop", JSONObject().apply { put("conversationId", conversationId) })
    }

    fun observeTypingStart(): Flow<JSONObject> = observeSocketEvent("typing:start")
    fun observeTypingStop(): Flow<JSONObject> = observeSocketEvent("typing:stop")
    fun observeMessageRead(): Flow<JSONObject> = observeSocketEvent("message:read")

    fun emitCallInvite(toUserId: String, conversationId: String, callType: String, onAck: (JSONObject) -> Unit) {
        val payload = JSONObject().apply {
            put("toUserId", toUserId); put("conversationId", conversationId); put("callType", callType)
        }
        socket?.emit("call:invite", payload, io.socket.client.Ack { args -> onAck(args[0] as JSONObject) })
    }

    fun emitCallAccept(toUserId: String, conversationId: String, callId: String?) {
        val payload = JSONObject().apply {
            put("toUserId", toUserId); put("conversationId", conversationId); put("callId", callId)
        }
        socket?.emit("call:accept", payload)
    }

    fun emitCallReject(toUserId: String, conversationId: String, callId: String?, reason: String = "declined") {
        val payload = JSONObject().apply {
            put("toUserId", toUserId); put("conversationId", conversationId); put("callId", callId); put("reason", reason)
        }
        socket?.emit("call:reject", payload)
    }

    fun emitCallOffer(toUserId: String, sdp: String, type: String) {
        val payload = JSONObject().apply {
            put("toUserId", toUserId)
            put("sdp", JSONObject().apply { put("sdp", sdp); put("type", type) })
        }
        socket?.emit("call:offer", payload)
    }

    fun emitCallAnswer(toUserId: String, sdp: String, type: String) {
        val payload = JSONObject().apply {
            put("toUserId", toUserId)
            put("sdp", JSONObject().apply { put("sdp", sdp); put("type", type) })
        }
        socket?.emit("call:answer", payload)
    }

    fun emitIceCandidate(toUserId: String, candidate: String, sdpMid: String?, sdpMLineIndex: Int) {
        val payload = JSONObject().apply {
            put("toUserId", toUserId)
            put("candidate", JSONObject().apply {
                put("candidate", candidate); put("sdpMid", sdpMid); put("sdpMLineIndex", sdpMLineIndex)
            })
        }
        socket?.emit("call:ice-candidate", payload)
    }

    fun emitCallEnd(toUserId: String, conversationId: String, callId: String?) {
        val payload = JSONObject().apply {
            put("toUserId", toUserId); put("conversationId", conversationId); put("callId", callId)
        }
        socket?.emit("call:end", payload)
    }

    fun observeCallIncoming(): Flow<JSONObject> = observeSocketEvent("call:incoming")
    fun observeCallAccepted(): Flow<JSONObject> = observeSocketEvent("call:accepted")
    fun observeCallRejected(): Flow<JSONObject> = observeSocketEvent("call:rejected")
    fun observeCallOffer(): Flow<JSONObject> = observeSocketEvent("call:offer")
    fun observeCallAnswer(): Flow<JSONObject> = observeSocketEvent("call:answer")
    fun observeCallIceCandidate(): Flow<JSONObject> = observeSocketEvent("call:ice-candidate")
    fun observeCallEnded(): Flow<JSONObject> = observeSocketEvent("call:ended")
}