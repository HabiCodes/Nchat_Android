package com.jarvis.nchat.presentation.calls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.MissedCallRed
import com.jarvis.nchat.core.designsystem.OutgoingCallGreen
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.presentation.components.AvatarImage

enum class CallDirection { INCOMING, OUTGOING, MISSED }
enum class CallKind { AUDIO, VIDEO }

data class CallLogEntry(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val otherUserId: String,
    val direction: CallDirection,
    val kind: CallKind,
    val time: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    onAudioCallClick: (String, String) -> Unit,
    viewModel: CallHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Calls", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.calls.isEmpty() -> Text(
                    "No calls yet",
                    modifier = Modifier.align(Alignment.Center),
                    color = ChatAppTheme.extendedColors.textSecondary
                )
                else -> LazyColumn(contentPadding = PaddingValues(vertical = Spacing.sm)) {
                    items(uiState.calls, key = { it.id }) { call ->
                        CallRow(call = call, onCallClick = { onAudioCallClick(call.otherUserId, call.name) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CallRow(call: CallLogEntry, onCallClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onCallClick).padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(name = call.name, avatarUrl = call.avatarUrl, size = 48)
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                call.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (call.direction == CallDirection.MISSED) MissedCallRed else MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val directionIcon = when (call.direction) {
                    CallDirection.INCOMING -> Icons.Filled.CallReceived
                    CallDirection.OUTGOING -> Icons.Filled.CallMade
                    CallDirection.MISSED -> Icons.Filled.CallMissed
                }
                val directionColor = if (call.direction == CallDirection.MISSED) MissedCallRed else OutgoingCallGreen
                Icon(directionIcon, contentDescription = null, tint = directionColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(Spacing.xs))
                Text(call.time.take(16).replace("T", " "), style = MaterialTheme.typography.bodySmall, color = ChatAppTheme.extendedColors.textSecondary)
            }
        }
        IconButton(onClick = onCallClick) {
            Icon(Icons.Filled.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
        }
    }
}