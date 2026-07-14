package com.jarvis.nchat.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.IncomingBubbleShape
import com.jarvis.nchat.core.designsystem.Spacing

@Composable
fun TypingIndicatorBubble() {
    val extended = ChatAppTheme.extendedColors
    Box(
        modifier = Modifier
            .clip(IncomingBubbleShape)
            .background(extended.bubbleIncoming)
            .padding(horizontal = Spacing.md, vertical = Spacing.md)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { index -> TypingDot(delayMillis = index * 150) }
        }
    }
}

@Composable
private fun TypingDot(delayMillis: Int) {
    val transition = rememberInfiniteTransition(label = "typing_dot")
    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = delayMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotOffset"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .offset(y = offsetY.dp)
            .clip(CircleShape)
            .background(ChatAppTheme.extendedColors.textSecondary)
    )
}