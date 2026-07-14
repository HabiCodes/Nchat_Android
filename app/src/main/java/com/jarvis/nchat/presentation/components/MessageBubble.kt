package com.jarvis.nchat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.IncomingBubbleShape
import com.jarvis.nchat.core.designsystem.OutgoingBubbleShape
import com.jarvis.nchat.core.designsystem.ReadTickBlue
import com.jarvis.nchat.core.designsystem.Spacing

enum class MessageStatus { SENDING, SENT, DELIVERED, READ }

@Composable
fun MessageBubble(
    content: String,
    time: String,
    isOutgoing: Boolean,
    status: MessageStatus,
    modifier: Modifier = Modifier,
) {
    val extended = ChatAppTheme.extendedColors
    val bubbleColor = if (isOutgoing) extended.bubbleOutgoing else extended.bubbleIncoming
    val textColor = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface
    val shape = if (isOutgoing) OutgoingBubbleShape else IncomingBubbleShape

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            Column {
                Text(text = content, color = textColor, style = MaterialTheme.typography.bodyLarge)
                Row(
                    modifier = Modifier.padding(top = Spacing.xs),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = time,
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.65f),
                    )
                    if (isOutgoing) {
                        Box(modifier = Modifier.padding(start = 4.dp))
                        MessageStatusIcon(status)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageStatusIcon(status: MessageStatus) {
    when (status) {
        MessageStatus.SENDING -> { /* could show a small clock icon here */ }
        MessageStatus.SENT -> Icon(Icons.Default.Done, contentDescription = "Sent", tint = Color.White.copy(alpha = 0.7f))
        MessageStatus.DELIVERED -> Icon(Icons.Default.DoneAll, contentDescription = "Delivered", tint = Color.White.copy(alpha = 0.7f))
        MessageStatus.READ -> Icon(Icons.Default.DoneAll, contentDescription = "Read", tint = ReadTickBlue)
    }
}