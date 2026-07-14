package com.jarvis.nchat.presentation.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.nchat.data.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallHistoryUiState(val calls: List<CallLogEntry> = emptyList(), val isLoading: Boolean = true)

@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    private val callRepository: CallRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CallHistoryUiState())
    val uiState: StateFlow<CallHistoryUiState> = _uiState

    init {
        viewModelScope.launch {
            callRepository.getCallHistory()
                .onSuccess { _uiState.value = CallHistoryUiState(calls = it, isLoading = false) }
                .onFailure { _uiState.value = CallHistoryUiState(isLoading = false) }
        }
    }
}