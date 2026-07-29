package com.jarvis.nchat.presentation.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.nchat.data.repository.CallHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallHistoryUiState(val calls: List<CallLogEntry> = emptyList(), val isLoading: Boolean = true)

@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    private val callHistoryRepository: CallHistoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CallHistoryUiState())
    val uiState: StateFlow<CallHistoryUiState> = _uiState

    init {
        viewModelScope.launch {
            callHistoryRepository.getCallHistory()
                .onSuccess { _uiState.value = CallHistoryUiState(calls = it, isLoading = false) }
                .onFailure { _uiState.value = CallHistoryUiState(isLoading = false) }
        }
    }
}