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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.core.designsystem.components.PasswordField
import com.jarvis.nchat.presentation.auth.components.ErrorCard
import com.jarvis.nchat.presentation.auth.components.NChatButton
import com.jarvis.nchat.presentation.auth.components.ScreenHeader


@Composable
fun ResetPasswordScreen(
    resetToken: String,
    onDone: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var newPassword by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onDone()
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
                    title = "Set a new password",
                    subtitle = "Choose a new password for your account",
                )
                Spacer(Modifier.height(Spacing.xxl))

                PasswordField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        viewModel.clearError()
                    },
                    label = "New password",
                    supportingText = "At least 6 characters",
                    enabled = !isLoading,
                    isError = errorMessage != null,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.confirmPasswordReset(resetToken, newPassword) },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Spacing.lg))

                ErrorCard(message = errorMessage, modifier = Modifier.fillMaxWidth())
                if (errorMessage != null) {
                    Spacer(Modifier.height(Spacing.md))
                }

                NChatButton(
                    text = "Reset password",
                    onClick = { viewModel.confirmPasswordReset(resetToken, newPassword) },
                    enabled = !isLoading,
                    isLoading = isLoading,
                )
            }
        }
    }
}