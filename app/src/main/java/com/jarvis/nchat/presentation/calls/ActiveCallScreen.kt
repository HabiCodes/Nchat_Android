package com.jarvis.nchat.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.ErrorRed
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.presentation.components.AvatarImage
import kotlinx.coroutines.delay
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext


@Composable
fun ActiveCallScreen(
    onCallEnded: () -> Unit,
    viewModel: CallViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(uiState.status) {
        if (uiState.status == CallStatus.ENDED) onCallEnded()
    }

    // Simple call-duration ticker, starts once connected
    LaunchedEffect(uiState.status == CallStatus.CONNECTED) {
        if (uiState.status == CallStatus.CONNECTED) {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(Spacing.xxxl))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AvatarImage(name = uiState.otherUsername, avatarUrl = null, size = 140)
            Spacer(Modifier.height(Spacing.lg))
            Text(uiState.otherUsername, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = when (uiState.status) {
                    CallStatus.CONNECTING -> "Connecting..."
                    CallStatus.RINGING_OUTGOING -> "Ringing..."
                    CallStatus.CONNECTED -> formatDuration(elapsedSeconds)
                    else -> ""
                },
                style = MaterialTheme.typography.bodyLarge,
                color = com.jarvis.nchat.core.designsystem.ChatAppTheme.extendedColors.textSecondary
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xxl)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xxl),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallToggleButton(
                    icon = if (uiState.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    isActive = uiState.isMuted,
                    onClick = { viewModel.toggleMute() }
                )
                CallToggleButton(
                    icon = Icons.Filled.VolumeUp,
                    isActive = uiState.isSpeakerOn,
                    onClick = { viewModel.toggleSpeaker(context) }
                )
            }
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                CallActionButton(
                    icon = Icons.Filled.CallEnd,
                    backgroundColor = ErrorRed,
                    onClick = { viewModel.endCall() },
                    size = 68
                )
            }
        }
    }
}

@Composable
private fun CallToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = null, tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}