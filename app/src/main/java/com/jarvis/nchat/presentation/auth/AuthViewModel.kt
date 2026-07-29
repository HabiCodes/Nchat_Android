package com.jarvis.nchat.presentation.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.nchat.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class RegisterOtpSent(val email: String) : AuthUiState()
    data class ResetOtpSent(val email: String) : AuthUiState()
    data class ResetVerified(val resetToken: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    // ---- Register: step 1, request OTP ----
    fun register(username: String, email: String, password: String) {
        val validationError = validateRegister(username, email, password)
        if (validationError != null) {
            _uiState.value = AuthUiState.Error(validationError)
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.registerRequest(username, email, password)
                .onSuccess { _uiState.value = AuthUiState.RegisterOtpSent(email) }
                .onFailure { _uiState.value = AuthUiState.Error(friendlyMessage(it)) }
        }
    }

    // ---- Register: step 2, verify OTP ----
    fun verifyRegisterOtp(email: String, code: String) {
        if (code.isBlank()) {
            _uiState.value = AuthUiState.Error("Enter the verification code")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.registerVerify(email, code)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Verification failed") }
        }
    }

    fun login(email: String, password: String) {
        val validationError = validateLogin(email, password)
        if (validationError != null) {
            _uiState.value = AuthUiState.Error(validationError)
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Login failed") }
        }
    }

    // ---- Forgot password: step 1, request OTP ----
    fun requestPasswordReset(email: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Enter a valid email address")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.forgotPasswordRequest(email)
                .onSuccess { _uiState.value = AuthUiState.ResetOtpSent(email) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Something went wrong") }
        }
    }

    // ---- Forgot password: step 2, verify OTP ----
    fun verifyPasswordResetOtp(email: String, code: String) {
        if (code.isBlank()) {
            _uiState.value = AuthUiState.Error("Enter the verification code")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.forgotPasswordVerify(email, code)
                .onSuccess { token -> _uiState.value = AuthUiState.ResetVerified(token) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Invalid or expired code") }
        }
    }

    // ---- Forgot password: step 3, set new password ----
    fun confirmPasswordReset(resetToken: String, newPassword: String) {
        if (newPassword.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.forgotPasswordConfirm(resetToken, newPassword)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Failed to reset password") }
        }
    }

    private fun friendlyMessage(throwable: Throwable): String = if (throwable is java.net.SocketTimeoutException) {
        "Server is waking up, please try again in a moment"
    } else {
        throwable.message ?: "Something went wrong"
    }

    private fun validateLogin(email: String, password: String): String? = when {
        email.isBlank() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address"
        password.isBlank() -> "Password is required"
        else -> null
    }

    private fun validateRegister(username: String, email: String, password: String): String? = when {
        username.isBlank() -> "Username is required"
        username.length < 3 -> "Username must be at least 3 characters"
        email.isBlank() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address"
        password.isBlank() -> "Password is required"
        password.length < 8 -> "Password must be at least 8 characters"
        else -> null
    }
}