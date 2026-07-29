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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.presentation.auth.components.ErrorCard
import com.jarvis.nchat.presentation.auth.components.LoadingOverlay
import com.jarvis.nchat.presentation.auth.components.NChatButton
import com.jarvis.nchat.core.designsystem.components.NChatTextField
import com.jarvis.nchat.core.designsystem.components.PasswordField
import com.jarvis.nchat.presentation.auth.components.ScreenHeader

@Composable
fun RegisterScreen(
    onOtpSent: (email: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is AuthUiState.RegisterOtpSent) onOtpSent(state.email)
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
                    title = "Create account",
                    subtitle = "Join NChat and start messaging",
                )
                Spacer(Modifier.height(Spacing.xxl))

                NChatTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        viewModel.clearError()
                    },
                    label = "Username",
                    enabled = !isLoading,
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Spacing.md))

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
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Spacing.md))

                PasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.clearError()
                    },
                    label = "Password",
                    supportingText = "At least 8 characters",
                    enabled = !isLoading,
                    isError = errorMessage != null,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.register(username.trim(), email.trim(), password) },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Spacing.lg))

                ErrorCard(message = errorMessage, modifier = Modifier.fillMaxWidth())
                if (errorMessage != null) {
                    Spacer(Modifier.height(Spacing.md))
                }

                NChatButton(
                    text = "Register",
                    onClick = { viewModel.register(username.trim(), email.trim(), password) },
                    enabled = !isLoading,
                    isLoading = isLoading,
                )
                Spacer(Modifier.height(Spacing.md))

                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                ) {
                    Text(
                        text = "Already have an account? Log in",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        LoadingOverlay(isVisible = false)
    }
}