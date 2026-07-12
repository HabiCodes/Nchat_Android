package com.jarvis.nchat.presentation.calls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.routes.designsystem.MissedCallRed
import com.jarvis.nchat.routes.designsystem.OutgoingCallGreen
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.presentation.components.AvatarImage

enum class CallDirection { INCOMING, OUTGOING, MISSED }
enum class CallKind { AUDIO, VIDEO }

data class CallLogEntry(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val direction: CallDirection,
    val kind: CallKind,
    val time: String,
)

private val mockCalls = listOf(
    CallLogEntry("c1", "Naveen (Partner)", null, CallDirection.OUTGOING, CallKind.VIDEO, "Today, 10:22 AM"),
    CallLogEntry("c2", "Bob", null, CallDirection.MISSED, CallKind.AUDIO, "Today, 9:15 AM"),
    CallLogEntry("c3", "RD Solar Team", null, CallDirection.INCOMING, CallKind.AUDIO, "Yesterday, 6:40 PM"),
    CallLogEntry("c4", "Mom", null, CallDirection.OUTGOING, CallKind.VIDEO, "Yesterday, 8:05 PM"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    onCallEntryClick: (String) -> Unit,
    onAudioCallClick: (String) -> Unit,
    onVideoCallClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Calls", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(vertical = Spacing.sm)
        ) {
            items(mockCalls, key = { it.id }) { call ->
                CallRow(
                    call = call,
                    onClick = { onCallEntryClick(call.id) },
                    onAudioClick = { onAudioCallClick(call.id) },
                    onVideoClick = { onVideoCallClick(call.id) }
                )
            }
        }
    }
}

@Composable
private fun CallRow(
    call: CallLogEntry,
    onClick: () -> Unit,
    onAudioClick: () -> Unit,
    onVideoClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
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
                val directionColor = when (call.direction) {
                    CallDirection.MISSED -> MissedCallRed
                    else -> OutgoingCallGreen
                }
                Icon(directionIcon, contentDescription = null, tint = directionColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(Spacing.xs))
                Text(call.time, style = MaterialTheme.typography.bodySmall, color = ChatAppTheme.extendedColors.textSecondary)
            }
        }
        IconButton(onClick = onVideoClick) {
            Icon(Icons.Filled.Videocam, contentDescription = "Video call", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onAudioClick) {
            Icon(Icons.Filled.Call, contentDescription = "Audio call", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
