package com.jarvis.nchat.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.routes.designsystem.ErrorRed
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.presentation.components.AvatarImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String,
    email: String,
    avatarUrl: String?,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarImage(name = username, avatarUrl = avatarUrl, size = 96)
                Text(username, style = MaterialTheme.typography.headlineMedium)
                Text(email, style = MaterialTheme.typography.bodyMedium, color = ChatAppTheme.extendedColors.textSecondary)
            }

            Divider(color = ChatAppTheme.extendedColors.divider)

            ProfileMenuItem(Icons.Filled.Key, "Account", "Privacy, security, change number", onSettingsClick)
            ProfileMenuItem(Icons.Filled.Notifications, "Notifications", "Message, group & call tones", onSettingsClick)
            ProfileMenuItem(Icons.Filled.Info, "Help", "Help center, contact us, privacy policy", onSettingsClick)
            ProfileMenuItem(Icons.Filled.Logout, "Log out", null, onLogoutClick, tint = ErrorRed)
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(Spacing.lg))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (title == "Log out") ErrorRed else MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ChatAppTheme.extendedColors.textSecondary)
            }
        }
        if (title != "Log out") {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = ChatAppTheme.extendedColors.textSecondary)
        }
    }
}