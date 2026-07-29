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

@Composable
fun ForgotPasswordScreen(
    onOtpSent: (email: String) -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is AuthUiState.ResetOtpSent) onOtpSent(state.email)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.Center,
            ) {
                ScreenHeader(
                    title = "Reset your password",
                    subtitle = "Enter your account email and we'll send a code",
                )
                Spacer(Modifier.height(Spacing.xxl))

                NChatTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        viewModel.clearError()
                    },
                    label = "Email",
                    placeholder = "you@example.com",
                    enabled = !isLoading,
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.requestPasswordReset(email.trim()) },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Spacing.lg))

                ErrorCard(message = errorMessage, modifier = Modifier.fillMaxWidth())
                if (errorMessage != null) {
                    Spacer(Modifier.height(Spacing.md))
                }

                NChatButton(
                    text = "Send code",
                    onClick = { viewModel.requestPasswordReset(email.trim()) },
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
                        text = "Back to login",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}