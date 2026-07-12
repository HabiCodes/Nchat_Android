package com.jarvis.nchat.core.network

import com.jarvis.nchat.data.model.MessageDto
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
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

    fun connect(token: String) {
        if (socket?.connected() == true) return
        val options = IO.Options().apply {
            auth = mapOf("token" to token)
            transports = arrayOf("websocket")
        }
        socket = IO.socket(SOCKET_URL, options)
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    // Emits every incoming "message:new" event as a parsed MessageDto
    fun observeNewMessages(): Flow<MessageDto> = callbackFlow {
        val listener = io.socket.emitter.Emitter.Listener { args ->
            val data = args[0] as JSONObject
            val messageJson = data.getJSONObject("message").toString()
            val dto = gson.fromJson(messageJson, MessageDto::class.java)
            trySend(dto)
        }
        socket?.on("message:new", listener)
        awaitClose { socket?.off("message:new", listener) }
    }

    fun sendMessage(conversationId: String, content: String, clientMsgId: String, onAck: (JSONObject) -> Unit) {
        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("content", content)
            put("clientMsgId", clientMsgId)
        }
        socket?.emit("message:send", payload, io.socket.client.Ack { args ->
            onAck(args[0] as JSONObject)
        })
    }

    fun markRead(conversationId: String) {
        val payload = JSONObject().apply { put("conversationId", conversationId) }
        socket?.emit("message:read", payload)
    }

    fun emitTypingStart(conversationId: String) {
        val payload = JSONObject().apply { put("conversationId", conversationId) }
        socket?.emit("typing:start", payload)
    }
}