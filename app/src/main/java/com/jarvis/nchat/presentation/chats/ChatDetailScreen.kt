package com.jarvis.nchat.presentation.chats

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
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
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.domain.model.MessageDeliveryStatus
import com.jarvis.nchat.presentation.calls.CallViewModel
import com.jarvis.nchat.presentation.components.AvatarImage
import com.jarvis.nchat.presentation.components.MessageBubble
import com.jarvis.nchat.presentation.components.MessageStatus
import com.jarvis.nchat.presentation.components.OnlineStatusDot
import com.jarvis.nchat.presentation.components.TypingIndicatorBubble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    contactName: String,
    otherUserId: String,
    conversationId: String,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    onStartCall: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel(),
) {
    var input by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Activity-scoped so call state survives navigating from this screen to ActiveCallScreen
    val activity = LocalContext.current as ComponentActivity
    val callViewModel: CallViewModel = hiltViewModel(activity)


    LaunchedEffect(uiState.messages.size, uiState.isOtherUserTyping) {
        val itemCount = uiState.messages.size + if (uiState.isOtherUserTyping) 1 else 0
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            callViewModel.startCall(otherUserId, contactName, conversationId)
            onStartCall()
        }
    }

    Scaffold(
        topBar = {
            ChatDetailTopBar(
                contactName = contactName,
                isOnline = isOnline,
                onBackClick = onBackClick,
                onAudioCallClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )
        },
        bottomBar = {
            MessageInputBar(
                value = input,
                onValueChange = { input = it; viewModel.notifyTyping() },
                onSend = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(input.trim())
                        input = ""
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(uiState.messages, key = { it.id }) { msg ->
                AnimatedVisibility(visible = true, enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 5 }) {
                    MessageBubble(
                        content = msg.content,
                        time = msg.createdAt.takeLast(8),
                        isOutgoing = msg.isMine,
                        status = msg.status.toUiStatus()
                    )
                }
            }
            if (uiState.isOtherUserTyping) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        TypingIndicatorBubble()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailTopBar(
    contactName: String,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    onAudioCallClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    AvatarImage(name = contactName, avatarUrl = null, size = 40)
                    if (isOnline) {
                        OnlineStatusDot(modifier = Modifier.align(Alignment.BottomEnd), size = 12)
                    }
                }
                Spacer(Modifier.width(Spacing.sm))
                Column {
                    Text(contactName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(
                        text = if (isOnline) "Active now" else "Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOnline) ChatAppTheme.extendedColors.online else ChatAppTheme.extendedColors.textSecondary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
        },
        actions = {
            IconButton(onClick = onAudioCallClick) {
                Icon(Icons.Filled.Call, contentDescription = "Audio call", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(Spacing.xs))
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().imePadding().background(MaterialTheme.colorScheme.surface).padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
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
            IconButton(onClick = onSend) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
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