package com.jarvis.nchat.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.presentation.components.AvatarImage
import com.jarvis.nchat.core.designsystem.ErrorRed
import androidx.compose.ui.unit.dp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPasswordSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLoggedOut()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().padding(Spacing.xxl), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarImage(name = uiState.user?.username ?: "?", avatarUrl = uiState.user?.avatarUrl, size = 96)
                    Text(uiState.user?.username ?: "", style = MaterialTheme.typography.headlineMedium)
                    Text(uiState.user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = ChatAppTheme.extendedColors.textSecondary)
                }
            }

            Divider(color = ChatAppTheme.extendedColors.divider)

            ProfileMenuItem(Icons.Filled.Key, "Change password", "Update your account password") { showPasswordSheet = true }
            ProfileMenuItem(Icons.Filled.Notifications, "Notifications", "Message, group & call tones") { }
            ProfileMenuItem(Icons.Filled.Info, "Help", "Help center, contact us, privacy policy") { }
            ProfileMenuItem(Icons.Filled.Logout, "Log out", null, tint = ErrorRed) { viewModel.logout() }
        }
    }

    if (showPasswordSheet) {
        ChangePasswordSheet(
            onDismiss = { showPasswordSheet = false; viewModel.resetPasswordState() },
            viewModel = viewModel,
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(Spacing.lg))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (title == "Log out") ErrorRed else MaterialTheme.colorScheme.onSurface)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ChatAppTheme.extendedColors.textSecondary) }
        }
        if (title != "Log out") Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = ChatAppTheme.extendedColors.textSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordSheet(onDismiss: () -> Unit, viewModel: ProfileViewModel) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    val state by viewModel.passwordState.collectAsState()

    LaunchedEffect(state) {
        if (state is PasswordChangeState.Success) onDismiss()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
            Text("Change password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Spacing.lg))
            OutlinedTextField(
                value = current, onValueChange = { current = it },
                label = { Text("Current password") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(
                value = new, onValueChange = { new = it },
                label = { Text("New password") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            if (state is PasswordChangeState.Error) {
                Spacer(Modifier.height(Spacing.sm))
                Text((state as PasswordChangeState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(Spacing.lg))
            Button(
                onClick = { viewModel.changePassword(current, new) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is PasswordChangeState.Loading
            ) {
                if (state is PasswordChangeState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Update password")
            }
            Spacer(Modifier.height(Spacing.xl))
        }
    }
}