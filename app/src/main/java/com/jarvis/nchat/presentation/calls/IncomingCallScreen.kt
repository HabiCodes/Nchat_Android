package com.jarvis.nchat.presentation.calls

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.presentation.components.AvatarImage
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
@Composable
fun IncomingCallScreen(
    onCallAccepted: () -> Unit,
    onCallDeclined: () -> Unit,
    viewModel: CallViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.status) {
        when (uiState.status) {
            CallStatus.CONNECTING, CallStatus.CONNECTED -> onCallAccepted()
            CallStatus.ENDED -> onCallDeclined()
            else -> {}
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "pulseScale"
    )

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
            Text("Incoming call", style = MaterialTheme.typography.bodyLarge, color = ChatAppTheme.extendedColors.textSecondary)
            Spacer(Modifier.height(Spacing.xxl))
            Box(modifier = Modifier.scale(pulseScale)) {
                AvatarImage(name = uiState.otherUsername, avatarUrl = null, size = 140)
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(uiState.otherUsername, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Nchat audio call", style = MaterialTheme.typography.bodyMedium, color = ChatAppTheme.extendedColors.textSecondary)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xxl),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallActionButton(
                icon = Icons.Filled.CallEnd,
                backgroundColor = com.jarvis.nchat.core.designsystem.ErrorRed,
                onClick = { viewModel.rejectCall() }
            )
            CallActionButton(
                icon = Icons.Filled.Call,
                backgroundColor = com.jarvis.nchat.core.designsystem.OutgoingCallGreen,
                onClick = { viewModel.acceptCall() }
            )
        }
    }
}

@Composable
fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    size: Int = 68,
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}