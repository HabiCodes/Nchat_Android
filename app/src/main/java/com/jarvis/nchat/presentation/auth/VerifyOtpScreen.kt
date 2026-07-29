package com.jarvis.nchat.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.core.designsystem.components.NChatTextField
import com.jarvis.nchat.presentation.auth.components.ErrorCard
import com.jarvis.nchat.presentation.auth.components.NChatButton
import com.jarvis.nchat.presentation.auth.components.ScreenHeader

/**
 * Shared OTP-entry UI used by both the register-verification and
 * password-reset-verification flows. The caller supplies the copy and the
 * verify action; this composable only owns the code input + loading/error
 * chrome, matching the visual language of Login/Register.
 */
@Composable
private fun OtpForm(
    email: String,
    isLoading: Boolean,
    errorMessage: String?,
    onCodeChanged: () -> Unit,
    onVerify: (code: String) -> Unit,
    onBack: () -> Unit,
) {
    var code by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.Center,
            ) {
                ScreenHeader(
                    title = "Check your email",
                    subtitle = "We sent a verification code to $email",
                )
                Spacer(Modifier.height(Spacing.xxl))

                NChatTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        onCodeChanged()
                    },
                    label = "Verification code",
                    enabled = !isLoading,
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onVerify(code.trim()) },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Spacing.lg))

                ErrorCard(message = errorMessage, modifier = Modifier.fillMaxWidth())
                if (errorMessage != null) {
                    Spacer(Modifier.height(Spacing.md))
                }

                NChatButton(
                    text = "Verify",
                    onClick = { onVerify(code.trim()) },
                    enabled = !isLoading,
                    isLoading = isLoading,
                )
                Spacer(Modifier.height(Spacing.md))

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                ) {
                    Text(
                        text = "Wrong email or no code received? Go back",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterVerifyScreen(
    email: String,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onVerified()
    }

    OtpForm(
        email = email,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onCodeChanged = { viewModel.clearError() },
        onVerify = { code -> viewModel.verifyRegisterOtp(email, code) },
        onBack = onBack,
    )
}

@Composable
fun ResetVerifyScreen(
    email: String,
    onVerified: (resetToken: String) -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is AuthUiState.ResetVerified) onVerified(state.resetToken)
    }

    OtpForm(
        email = email,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onCodeChanged = { viewModel.clearError() },
        onVerify = { code -> viewModel.verifyPasswordResetOtp(email, code) },
        onBack = onBack,
    )
}