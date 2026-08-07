package com.jarvis.nchat.presentation.chats

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.jarvis.nchat.core.designsystem.Spacing

@Composable
fun ChatHeader(title: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}