package com.jarvis.nchat.presentation.chats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.nchat.data.repository.ChatRepository
import com.jarvis.nchat.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatDetailUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState

    init {
        loadHistory()
        observeLiveMessages()
        chatRepository.markConversationRead(conversationId)
    }

    private fun loadHistory() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            chatRepository.getMessages(conversationId)
                .onSuccess { _uiState.value = ChatDetailUiState(messages = it) }
                .onFailure { _uiState.value = ChatDetailUiState(error = it.message) }
        }
    }

    private fun observeLiveMessages() {
        viewModelScope.launch {
            chatRepository.observeIncomingMessages(conversationId).collect { incoming ->
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + incoming
                )
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(conversationId, content)
                .onSuccess { sent ->
                    _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + sent)
                }
                .onFailure { /* TODO: surface send failure in UI */ }
        }
    }

    fun notifyTyping() = chatRepository.emitTyping(conversationId)
}