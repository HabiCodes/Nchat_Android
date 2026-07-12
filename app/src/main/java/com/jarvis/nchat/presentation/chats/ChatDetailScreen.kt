package com.jarvis.nchat.presentation.chats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.domain.model.Message
import com.jarvis.nchat.domain.model.MessageDeliveryStatus
import com.jarvis.nchat.presentation.components.MessageBubble
import com.jarvis.nchat.presentation.components.MessageStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    contactName: String,
    onBackClick: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel(),
) {
    var input by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contactName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().imePadding().background(MaterialTheme.colorScheme.surface).padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; viewModel.notifyTyping() },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    )
                )
                Spacer(Modifier.width(Spacing.sm))
                Box(
                    modifier = Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        if (input.isNotBlank()) {
                            viewModel.sendMessage(input.trim())
                            input = ""
                        }
                    }) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(uiState.messages, key = { it.id }) { msg ->
                AnimatedVisibility(visible = true, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 4 }) {
                    MessageBubble(
                        content = msg.content,
                        time = msg.createdAt.takeLast(8),
                        isOutgoing = msg.isMine,
                        status = msg.status.toUiStatus()
                    )
                }
            }
        }
    }
}

private fun MessageDeliveryStatus.toUiStatus() = when (this) {
    MessageDeliveryStatus.SENDING -> MessageStatus.SENDING
    MessageDeliveryStatus.SENT -> MessageStatus.SENT
    MessageDeliveryStatus.DELIVERED -> MessageStatus.DELIVERED
    MessageDeliveryStatus.READ -> MessageStatus.READ
}