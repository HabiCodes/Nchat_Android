package com.jarvis.nchat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jarvis.nchat.routes.designsystem.OnlineGreen

@Composable
fun OnlineStatusDot(modifier: Modifier = Modifier, size: Int = 14) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(OnlineGreen, CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
    )
}