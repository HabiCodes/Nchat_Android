package com.jarvis.nchat.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.nchat.data.repository.AuthRepository
import com.jarvis.nchat.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val loggedOut: Boolean = false,
)

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    object Success : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _passwordState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordState: StateFlow<PasswordChangeState> = _passwordState

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { _uiState.value = ProfileUiState(user = it, isLoading = false) }
                .onFailure { _uiState.value = ProfileUiState(isLoading = false) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value.copy(loggedOut = true)
        }
    }

    fun changePassword(current: String, new: String) {
        _passwordState.value = PasswordChangeState.Loading
        viewModelScope.launch {
            authRepository.changePassword(current, new)
                .onSuccess { _passwordState.value = PasswordChangeState.Success }
                .onFailure { _passwordState.value = PasswordChangeState.Error(it.message ?: "Failed") }
        }
    }

    fun resetPasswordState() { _passwordState.value = PasswordChangeState.Idle }
}