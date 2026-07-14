package com.jarvis.nchat.presentation.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.nchat.data.repository.ChatRepository
import com.jarvis.nchat.domain.model.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val cached = chatRepository.conversationsCache.value
    private val _uiState = MutableStateFlow(
        ChatListUiState(conversations = cached, isLoading = cached.isEmpty())
    )
    val uiState: StateFlow<ChatListUiState> = _uiState

    init { loadConversations() }

    fun loadConversations() {
        // Only show the spinner if we have nothing cached to show meanwhile
        if (_uiState.value.conversations.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            chatRepository.getConversations()
                .onSuccess { _uiState.value = ChatListUiState(conversations = it) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
        }
    }
}