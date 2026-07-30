package com.jarvis.nchat.presentation.chats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.nchat.core.chat.ActiveConversationTracker
import com.jarvis.nchat.core.network.SocketManager
import com.jarvis.nchat.data.repository.ChatRepository
import com.jarvis.nchat.domain.model.Message
import com.jarvis.nchat.domain.model.MessageDeliveryStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatDetailUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOtherUserTyping: Boolean = false,
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val socketManager: SocketManager,
    private val activeConversationTracker: ActiveConversationTracker,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState

    private var typingStopJob: Job? = null

    init {
        activeConversationTracker.activeConversationId = conversationId
        loadHistory()
        observeLiveMessages()
        observeReadReceipts()
        observeTypingIndicator()
        chatRepository.markConversationRead(conversationId)
    }

    override fun onCleared() {
        super.onCleared()
        if (activeConversationTracker.activeConversationId == conversationId) {
            activeConversationTracker.activeConversationId = null
        }
    }

    private fun loadHistory() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            chatRepository.getMessages(conversationId)
                .onSuccess { _uiState.value = _uiState.value.copy(messages = it, isLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
        }
    }

    private fun observeLiveMessages() {
        viewModelScope.launch {
            chatRepository.observeIncomingMessages(conversationId).collect { incoming ->
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + incoming,
                    isOtherUserTyping = false, // a real message arriving always clears typing state
                )
                // Mark it read immediately since the conversation is open
                chatRepository.markConversationRead(conversationId)
            }
        }
    }

    // Updates ticks on OUR sent messages to "read" once the other person opens the chat
    private fun observeReadReceipts() {
        viewModelScope.launch {
            socketManager.observeMessageRead().collect { data ->
                if (data.getString("conversationId") != conversationId) return@collect
                val readIds = data.getJSONArray("messageIds")
                val readIdSet = (0 until readIds.length()).map { readIds.getString(it) }.toSet()

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages.map { msg ->
                        if (msg.id in readIdSet) msg.copy(status = MessageDeliveryStatus.READ) else msg
                    }
                )
            }
        }
    }

    private fun observeTypingIndicator() {
        viewModelScope.launch {
            socketManager.observeTypingStart().collect { data ->
                if (data.optString("conversationId") == conversationId) {
                    _uiState.value = _uiState.value.copy(isOtherUserTyping = true)
                }
            }
        }
        viewModelScope.launch {
            socketManager.observeTypingStop().collect { data ->
                if (data.optString("conversationId") == conversationId) {
                    _uiState.value = _uiState.value.copy(isOtherUserTyping = false)
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(conversationId, content)
                .onSuccess { sent -> _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + sent) }
                .onFailure { /* TODO: surface send failure in UI */ }
        }
    }

    // Debounced typing: emits typing:start once, then typing:stop automatically after 2s of silence
    fun notifyTyping() {
        chatRepository.emitTyping(conversationId)
        typingStopJob?.cancel()
        typingStopJob = viewModelScope.launch {
            delay(2000)
            socketManager.emitTypingStop(conversationId)
        }
    }
}